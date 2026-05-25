const supabase = require('../../lib/supabase');
const { hashPassword } = require('../auth/auth.service');
const { newWorkerCode, newTempPassword } = require('../../utils/codes');
const { NotFound, Conflict, BadRequest } = require('../../utils/errors');
const { uploadBuffer, destroy, publicIdFromUrl } = require('../../lib/cloudinary');

const WORKER_SELECT = `
  id, user_id, role_title, monthly_salary,
  date_of_birth, gender, address, city, state,
  emergency_contact_name, emergency_contact_phone,
  id_proof_type, id_proof_number,
  photo_url, aadhaar_front_url, aadhaar_back_url,
  joining_date, leaving_date, status, notes, created_at, updated_at,
  user:users!workers_user_id_fkey ( id, user_code, full_name, email, phone, is_active, created_at )
`;

const cleanInput = (obj) => {
  const out = {};
  for (const [k, v] of Object.entries(obj)) out[k] = v === '' ? null : v;
  return out;
};

const generateUniqueWorkerCode = async () => {
  for (let i = 0; i < 5; i++) {
    const code = newWorkerCode();
    const { data } = await supabase.from('users').select('id').eq('user_code', code).maybeSingle();
    if (!data) return code;
  }
  throw new Error('Could not allocate unique worker code');
};

const create = async (input) => {
  const body = cleanInput(input);

  if (body.email) {
    const { data: exists } = await supabase.from('users').select('id').eq('email', body.email).maybeSingle();
    if (exists) throw Conflict('A user with that email already exists');
  }

  const userCode = await generateUniqueWorkerCode();
  const tempPassword = newTempPassword();
  const passwordHash = await hashPassword(tempPassword);

  const { data: user, error: uerr } = await supabase.from('users').insert({
    user_code: userCode,
    email: body.email || null,
    phone: body.phone || null,
    password_hash: passwordHash,
    role: 'worker',
    full_name: body.full_name,
    is_active: true,
  }).select().single();
  if (uerr) throw uerr;

  const workerInsert = {
    user_id: user.id,
    role_title: body.role_title || null,
    monthly_salary: body.monthly_salary ?? 0,
    joining_date: body.joining_date,
    leaving_date: body.leaving_date || null,
    date_of_birth: body.date_of_birth || null,
    gender: body.gender || null,
    address: body.address || null,
    city: body.city || null,
    state: body.state || null,
    emergency_contact_name: body.emergency_contact_name || null,
    emergency_contact_phone: body.emergency_contact_phone || null,
    id_proof_type: body.id_proof_type || null,
    id_proof_number: body.id_proof_number || null,
    notes: body.notes || null,
  };

  const { data: worker, error: werr } = await supabase
    .from('workers').insert(workerInsert).select(WORKER_SELECT).single();
  if (werr) {
    await supabase.from('users').delete().eq('id', user.id);
    throw werr;
  }
  return { worker, credentials: { user_code: userCode, temp_password: tempPassword } };
};

const list = async ({ q, status }) => {
  let query = supabase.from('workers').select(WORKER_SELECT).order('created_at', { ascending: false });
  if (status !== 'all') query = query.eq('status', status);
  const { data, error } = await query;
  if (error) throw error;
  let rows = data || [];
  if (q && q.trim()) {
    const needle = q.trim().toLowerCase();
    rows = rows.filter((w) => {
      const u = w.user || {};
      return (
        (u.full_name || '').toLowerCase().includes(needle) ||
        (u.user_code || '').toLowerCase().includes(needle) ||
        (u.phone || '').toLowerCase().includes(needle) ||
        (w.role_title || '').toLowerCase().includes(needle)
      );
    });
  }
  return rows;
};

const getById = async (id) => {
  const { data, error } = await supabase.from('workers').select(WORKER_SELECT).eq('id', id).maybeSingle();
  if (error) throw error;
  if (!data) throw NotFound('Worker not found');
  return data;
};

const update = async (id, input) => {
  const body = cleanInput(input);
  const existing = await getById(id);

  const userPatch = {};
  if ('full_name' in body) userPatch.full_name = body.full_name;
  if ('email' in body) userPatch.email = body.email || null;
  if ('phone' in body) userPatch.phone = body.phone || null;
  if (Object.keys(userPatch).length > 0) {
    if (userPatch.email) {
      const { data: clash } = await supabase.from('users').select('id')
        .eq('email', userPatch.email).neq('id', existing.user_id).maybeSingle();
      if (clash) throw Conflict('A user with that email already exists');
    }
    const { error: uerr } = await supabase.from('users').update(userPatch).eq('id', existing.user_id);
    if (uerr) throw uerr;
  }

  const workerPatch = {};
  const fields = [
    'role_title', 'monthly_salary', 'joining_date', 'leaving_date',
    'date_of_birth', 'gender', 'address', 'city', 'state',
    'emergency_contact_name', 'emergency_contact_phone',
    'id_proof_type', 'id_proof_number', 'notes', 'status',
  ];
  for (const f of fields) if (f in body) workerPatch[f] = body[f] ?? null;
  if ('monthly_salary' in workerPatch && workerPatch.monthly_salary == null) workerPatch.monthly_salary = 0;

  if (Object.keys(workerPatch).length > 0) {
    const { error } = await supabase.from('workers').update(workerPatch).eq('id', id);
    if (error) throw error;
  }
  return await getById(id);
};

const remove = async (id) => {
  const existing = await getById(id);
  for (const url of [existing.photo_url, existing.aadhaar_front_url, existing.aadhaar_back_url]) {
    try { const pid = publicIdFromUrl(url); if (pid) await destroy(pid); } catch (_) { /* tolerate */ }
  }
  try {
    await supabase.from('worker_salary_payments').update({ recorded_by: null }).eq('recorded_by', existing.user_id);
  } catch (_) { /* tolerate */ }
  const { error } = await supabase.from('users').delete().eq('id', existing.user_id);
  if (error) {
    if (error.code === '23503' || (error.message || '').toLowerCase().includes('foreign key')) {
      throw Conflict('Cannot delete this worker because some records still reference them.');
    }
    throw error;
  }
  return { id };
};

const uploadDocument = async (workerId, field, file) => {
  if (!file) throw BadRequest('No file uploaded');
  if (!['photo_url', 'aadhaar_front_url', 'aadhaar_back_url'].includes(field)) {
    throw BadRequest(`Unsupported field: ${field}`);
  }
  const existing = await getById(workerId);
  const publicId = `workers/${workerId}/${field}`;
  const result = await uploadBuffer(file.buffer, { publicId });

  const oldPid = publicIdFromUrl(existing[field]);
  if (oldPid && oldPid !== result.public_id) await destroy(oldPid);

  const { error } = await supabase.from('workers').update({ [field]: result.secure_url }).eq('id', workerId);
  if (error) throw error;
  return { url: result.secure_url, public_id: result.public_id };
};

const getCredentials = async (workerId) => {
  const w = await getById(workerId);
  return {
    worker_id: w.id,
    user_id: w.user.id,
    user_code: w.user.user_code,
    email: w.user.email,
    full_name: w.user.full_name,
    phone: w.user.phone,
  };
};

const resetPassword = async (workerId, newPassword) => {
  const w = await getById(workerId);
  const password = (newPassword && String(newPassword).trim().length >= 6)
    ? String(newPassword).trim() : newTempPassword();
  const password_hash = await hashPassword(password);
  const { error } = await supabase.from('users').update({ password_hash }).eq('id', w.user.id);
  if (error) throw error;
  await supabase.from('refresh_tokens').update({ revoked_at: new Date().toISOString() })
    .eq('user_id', w.user.id).is('revoked_at', null);
  return { user_code: w.user.user_code, email: w.user.email, new_password: password };
};

const recordSalary = async (workerId, input, recordedBy) => {
  const w = await getById(workerId);
  const monthDate = `${input.pay_for_month}-01`;
  const { data, error } = await supabase.from('worker_salary_payments').insert({
    worker_id: w.id,
    amount: input.amount,
    pay_for_month: monthDate,
    method: input.method,
    paid_at: input.paid_at || new Date().toISOString(),
    reference: input.reference || null,
    notes: input.notes || null,
    recorded_by: recordedBy?.id || null,
  }).select().single();
  if (error) throw error;
  return data;
};

const listSalaryPayments = async ({ worker_id, month }) => {
  let q = supabase.from('worker_salary_payments').select('*').order('paid_at', { ascending: false });
  if (worker_id) q = q.eq('worker_id', worker_id);
  if (month) q = q.eq('pay_for_month', `${month}-01`);
  const { data, error } = await q;
  if (error) throw error;
  return data || [];
};

module.exports = {
  create, list, getById, update, remove,
  uploadDocument, getCredentials, resetPassword,
  recordSalary, listSalaryPayments,
};
