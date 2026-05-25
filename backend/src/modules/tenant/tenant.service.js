const supabase = require('../../lib/supabase');
const { hashPassword } = require('../auth/auth.service');
const { newTenantCode, newTempPassword } = require('../../utils/codes');
const { NotFound, Conflict, BadRequest } = require('../../utils/errors');
const Joi = require('joi');
const { uploadBuffer, destroy, publicIdFromUrl } = require('../../lib/cloudinary');

const TENANT_SELECT = `
  id, user_id, room_id, bed_id,
  date_of_birth, gender, address, city, state,
  emergency_contact_name, emergency_contact_phone,
  occupation, id_proof_type, id_proof_number,
  photo_url, aadhaar_front_url, aadhaar_back_url,
  joining_date, leaving_date, monthly_rent, security_deposit,
  status, notes, created_at, updated_at,
  user:users!tenants_user_id_fkey ( id, user_code, full_name, email, phone, is_active, created_at ),
  room:rooms ( id, room_number, floor, monthly_rent, status ),
  bed:beds ( id, bed_label, status )
`;

const cleanInput = (obj) => {
  const out = {};
  for (const [k, v] of Object.entries(obj)) {
    out[k] = v === '' ? null : v;
  }
  return out;
};

// Generate a unique tenant user_code (handle the unlikely collision)
const generateUniqueUserCode = async () => {
  for (let i = 0; i < 5; i++) {
    const code = newTenantCode();
    const { data } = await supabase.from('users').select('id').eq('user_code', code).maybeSingle();
    if (!data) return code;
  }
  throw new Error('Could not allocate unique tenant code');
};

const create = async (input) => {
  const body = cleanInput(input);

  // Optional uniqueness checks
  if (body.email) {
    const { data: exists } = await supabase.from('users').select('id').eq('email', body.email).maybeSingle();
    if (exists) throw Conflict('A user with that email already exists');
  }

  if (body.bed_id) {
    const { data: bed } = await supabase.from('beds').select('id, status').eq('id', body.bed_id).maybeSingle();
    if (!bed) throw NotFound('Bed not found');
    if (bed.status === 'occupied') throw Conflict('Bed is already occupied');
  }

  const userCode = await generateUniqueUserCode();
  const tempPassword = newTempPassword();
  const passwordHash = await hashPassword(tempPassword);

  const { data: user, error: uerr } = await supabase
    .from('users')
    .insert({
      user_code: userCode,
      email: body.email || null,
      phone: body.phone || null,
      password_hash: passwordHash,
      role: 'tenant',
      full_name: body.full_name,
      is_active: true,
    })
    .select()
    .single();
  if (uerr) throw uerr;

  const tenantInsert = {
    user_id: user.id,
    room_id: body.room_id || null,
    bed_id: body.bed_id || null,
    date_of_birth: body.date_of_birth || null,
    gender: body.gender || null,
    address: body.address || null,
    city: body.city || null,
    state: body.state || null,
    emergency_contact_name: body.emergency_contact_name || null,
    emergency_contact_phone: body.emergency_contact_phone || null,
    occupation: body.occupation || null,
    id_proof_type: body.id_proof_type || null,
    id_proof_number: body.id_proof_number || null,
    joining_date: body.joining_date,
    leaving_date: body.leaving_date || null,
    monthly_rent: body.monthly_rent ?? null,
    security_deposit: body.security_deposit ?? 0,
    notes: body.notes || null,
  };

  const { data: tenant, error: terr } = await supabase
    .from('tenants')
    .insert(tenantInsert)
    .select(TENANT_SELECT)
    .single();
  if (terr) {
    // Roll back the user we just inserted so we don't leak orphans
    await supabase.from('users').delete().eq('id', user.id);
    throw terr;
  }

  // Mark the bed as occupied if assigned
  if (body.bed_id) {
    await supabase.from('beds').update({ status: 'occupied' }).eq('id', body.bed_id);
    await refreshRoomStatus(tenant.room_id);
  }

  return {
    tenant,
    credentials: { user_code: userCode, temp_password: tempPassword },
  };
};

const list = async ({ q, status, room_id, page, page_size }) => {
  let query = supabase
    .from('tenants')
    .select(TENANT_SELECT, { count: 'exact' })
    .order('created_at', { ascending: false });

  if (status !== 'all') query = query.eq('status', status);
  if (room_id) query = query.eq('room_id', room_id);

  const from = (page - 1) * page_size;
  const to = from + page_size - 1;
  query = query.range(from, to);

  const { data, error, count } = await query;
  if (error) throw error;

  let rows = data || [];
  if (q && q.trim()) {
    const needle = q.trim().toLowerCase();
    rows = rows.filter((t) => {
      const u = t.user || {};
      return (
        (u.full_name || '').toLowerCase().includes(needle) ||
        (u.email || '').toLowerCase().includes(needle) ||
        (u.phone || '').toLowerCase().includes(needle) ||
        (u.user_code || '').toLowerCase().includes(needle)
      );
    });
  }

  return { rows, total: count ?? rows.length, page, page_size };
};

const getById = async (id) => {
  const { data, error } = await supabase
    .from('tenants').select(TENANT_SELECT).eq('id', id).maybeSingle();
  if (error) throw error;
  if (!data) throw NotFound('Tenant not found');
  return data;
};

const update = async (id, input) => {
  const body = cleanInput(input);
  const existing = await getById(id);

  // user-table fields go to users
  const userPatch = {};
  if ('full_name' in body) userPatch.full_name = body.full_name;
  if ('email' in body) userPatch.email = body.email || null;
  if ('phone' in body) userPatch.phone = body.phone || null;
  if (Object.keys(userPatch).length > 0) {
    if (userPatch.email) {
      const { data: clash } = await supabase
        .from('users').select('id').eq('email', userPatch.email).neq('id', existing.user_id).maybeSingle();
      if (clash) throw Conflict('A user with that email already exists');
    }
    const { error: uerr } = await supabase.from('users').update(userPatch).eq('id', existing.user_id);
    if (uerr) throw uerr;
  }

  // Bed change handling
  let bedChanged = false;
  if ('bed_id' in body && body.bed_id !== existing.bed_id) {
    if (body.bed_id) {
      const { data: bed } = await supabase.from('beds').select('id, status').eq('id', body.bed_id).maybeSingle();
      if (!bed) throw NotFound('Bed not found');
      if (bed.status === 'occupied') throw Conflict('Target bed is already occupied');
    }
    if (existing.bed_id) await supabase.from('beds').update({ status: 'vacant' }).eq('id', existing.bed_id);
    if (body.bed_id) await supabase.from('beds').update({ status: 'occupied' }).eq('id', body.bed_id);
    bedChanged = true;
  }

  const tenantPatch = {};
  const tenantFields = [
    'room_id', 'bed_id', 'date_of_birth', 'gender', 'address', 'city', 'state',
    'emergency_contact_name', 'emergency_contact_phone', 'occupation', 'id_proof_type',
    'id_proof_number', 'joining_date', 'leaving_date', 'monthly_rent', 'security_deposit',
    'notes', 'status',
  ];
  for (const f of tenantFields) {
    if (f in body) tenantPatch[f] = body[f] ?? null;
  }
  if ('security_deposit' in tenantPatch && tenantPatch.security_deposit == null) tenantPatch.security_deposit = 0;

  if (Object.keys(tenantPatch).length > 0) {
    const { error } = await supabase.from('tenants').update(tenantPatch).eq('id', id);
    if (error) throw error;
  }

  if (bedChanged) {
    await refreshRoomStatus(existing.room_id);
    if (tenantPatch.room_id) await refreshRoomStatus(tenantPatch.room_id);
  } else if ('room_id' in tenantPatch && tenantPatch.room_id !== existing.room_id) {
    await refreshRoomStatus(existing.room_id);
    await refreshRoomStatus(tenantPatch.room_id);
  }

  return await getById(id);
};

const remove = async (id) => {
  const existing = await getById(id);

  // Best-effort: free bed (don't let this block delete)
  if (existing.bed_id) {
    try {
      await supabase.from('beds').update({ status: 'vacant' }).eq('id', existing.bed_id);
    } catch (_) { /* tolerate */ }
  }

  // Best-effort: clean up Cloudinary assets
  for (const url of [existing.photo_url, existing.aadhaar_front_url, existing.aadhaar_back_url]) {
    try {
      const pid = publicIdFromUrl(url);
      if (pid) await destroy(pid);
    } catch (_) { /* tolerate */ }
  }

  // Best-effort: null out FK columns on related rows that don't cascade (so the
  // user delete never gets blocked by referential integrity)
  try {
    await supabase.from('payments').update({ recorded_by: null }).eq('recorded_by', existing.user_id);
    await supabase.from('visitor_logs').update({ recorded_by: null }).eq('recorded_by', existing.user_id);
    await supabase.from('invoices').update({ generated_by: null }).eq('generated_by', existing.user_id);
  } catch (_) { /* tolerate */ }

  // Deleting the user cascades to tenant row via FK (and from there to bills,
  // payments, complaints, etc. via on delete cascade).
  const { error } = await supabase.from('users').delete().eq('id', existing.user_id);
  if (error) {
    // Translate the obscure Postgres FK message into something the admin can act on
    if (error.code === '23503' || (error.message || '').toLowerCase().includes('foreign key')) {
      throw Conflict('Cannot delete this member because some records still reference them. Please contact support.');
    }
    throw error;
  }

  if (existing.room_id) {
    try { await refreshRoomStatus(existing.room_id); } catch (_) { /* tolerate */ }
  }
  return { id };
};

const uploadDocument = async (tenantId, field, file) => {
  if (!file) throw BadRequest('No file uploaded');
  if (!['photo_url', 'aadhaar_front_url', 'aadhaar_back_url'].includes(field)) {
    throw BadRequest(`Unsupported field: ${field}`);
  }
  const existing = await getById(tenantId);

  const publicId = `tenants/${tenantId}/${field}`;
  const result = await uploadBuffer(file.buffer, { publicId });

  // Delete old asset (best-effort) if it points to a different public_id
  const oldPid = publicIdFromUrl(existing[field]);
  if (oldPid && oldPid !== result.public_id) {
    await destroy(oldPid);
  }

  const { error } = await supabase
    .from('tenants').update({ [field]: result.secure_url }).eq('id', tenantId);
  if (error) throw error;

  return { url: result.secure_url, public_id: result.public_id };
};

// Keeps rooms.status accurate when occupancy changes
const refreshRoomStatus = async (roomId) => {
  if (!roomId) return;
  const { data: room } = await supabase.from('rooms').select('id, capacity').eq('id', roomId).maybeSingle();
  if (!room) return;
  const { count } = await supabase
    .from('beds')
    .select('id', { count: 'exact', head: true })
    .eq('room_id', roomId)
    .eq('status', 'occupied');
  let status = 'vacant';
  if (count >= room.capacity) status = 'occupied';
  else if (count > 0) status = 'partial';
  await supabase.from('rooms').update({ status }).eq('id', roomId);
};

const getCredentials = async (tenantId) => {
  const tenant = await getById(tenantId);
  return {
    tenant_id: tenant.id,
    user_id: tenant.user.id,
    user_code: tenant.user.user_code,
    email: tenant.user.email,
    full_name: tenant.user.full_name,
    phone: tenant.user.phone,
  };
};

const resetMemberPassword = async (tenantId, newPassword) => {
  const tenant = await getById(tenantId);
  const password = (newPassword && String(newPassword).trim().length >= 6)
    ? String(newPassword).trim()
    : newTempPassword();
  const password_hash = await hashPassword(password);
  const { error } = await supabase.from('users').update({ password_hash }).eq('id', tenant.user.id);
  if (error) throw error;
  // Revoke existing refresh tokens
  await supabase.from('refresh_tokens').update({ revoked_at: new Date().toISOString() })
    .eq('user_id', tenant.user.id).is('revoked_at', null);
  return { user_code: tenant.user.user_code, email: tenant.user.email, new_password: password };
};

module.exports = { create, list, getById, update, remove, uploadDocument, refreshRoomStatus, getCredentials, resetMemberPassword };
