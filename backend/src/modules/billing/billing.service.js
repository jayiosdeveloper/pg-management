const supabase = require('../../lib/supabase');
const { NotFound, BadRequest, Conflict } = require('../../utils/errors');

const BILL_SELECT = `
  id, tenant_id, category, amount, amount_paid,
  billing_month, due_date, description, status, created_at, updated_at,
  tenant:tenants ( id, user_id, room_id, monthly_rent, user:users!tenants_user_id_fkey ( id, full_name, user_code, email, phone ), room:rooms ( id, room_number ) )
`;

const normalizeMonth = (m) => /^\d{4}-\d{2}$/.test(m) ? `${m}-01` : m;

const create = async (input) => {
  const billing_month = normalizeMonth(input.billing_month);
  const { data: tenant } = await supabase.from('tenants').select('id, status').eq('id', input.tenant_id).maybeSingle();
  if (!tenant) throw NotFound('Tenant not found');

  const { data: existing } = await supabase
    .from('bills')
    .select('id')
    .eq('tenant_id', input.tenant_id)
    .eq('category', input.category)
    .eq('billing_month', billing_month)
    .maybeSingle();
  if (existing) throw Conflict('A bill in this category already exists for this month');

  const { data, error } = await supabase.from('bills').insert({
    tenant_id: input.tenant_id,
    category: input.category,
    amount: input.amount,
    billing_month,
    due_date: input.due_date,
    description: input.description || null,
  }).select(BILL_SELECT).single();
  if (error) throw error;
  return data;
};

const list = async (params, currentUser) => {
  const { tenant_id, status, category, month, page, page_size } = params;

  let query = supabase.from('bills').select(BILL_SELECT, { count: 'exact' }).order('billing_month', { ascending: false }).order('created_at', { ascending: false });

  // Tenant role: scope to their own tenant row
  if (currentUser?.role === 'tenant') {
    const { data: t } = await supabase.from('tenants').select('id').eq('user_id', currentUser.id).maybeSingle();
    if (!t) return { rows: [], total: 0, page, page_size };
    query = query.eq('tenant_id', t.id);
  } else if (tenant_id) {
    query = query.eq('tenant_id', tenant_id);
  }

  if (status !== 'all') query = query.eq('status', status);
  if (category !== 'all') query = query.eq('category', category);
  if (month) query = query.eq('billing_month', `${month}-01`);

  const from = (page - 1) * page_size;
  query = query.range(from, from + page_size - 1);

  const { data, error, count } = await query;
  if (error) throw error;
  return { rows: data || [], total: count ?? 0, page, page_size };
};

const getById = async (id, currentUser) => {
  const { data, error } = await supabase.from('bills').select(BILL_SELECT).eq('id', id).maybeSingle();
  if (error) throw error;
  if (!data) throw NotFound('Bill not found');
  if (currentUser?.role === 'tenant') {
    const { data: t } = await supabase.from('tenants').select('id').eq('user_id', currentUser.id).maybeSingle();
    if (!t || t.id !== data.tenant_id) throw NotFound('Bill not found');
  }
  return data;
};

const update = async (id, input) => {
  await getById(id);
  const { data, error } = await supabase.from('bills').update({
    ...(input.amount !== undefined ? { amount: input.amount } : {}),
    ...(input.due_date !== undefined ? { due_date: input.due_date } : {}),
    ...(input.description !== undefined ? { description: input.description } : {}),
  }).eq('id', id).select(BILL_SELECT).single();
  if (error) throw error;
  return data;
};

const remove = async (id) => {
  const { error } = await supabase.from('bills').delete().eq('id', id);
  if (error) throw error;
};

// Records a payment against a bill. Updates amount_paid; trigger derives status.
const recordPayment = async (billId, input, recordedBy) => {
  const bill = await getById(billId);
  const newPaid = Number(bill.amount_paid) + Number(input.amount);
  if (newPaid > Number(bill.amount) + 0.001) throw BadRequest(`Payment exceeds remaining due (${(bill.amount - bill.amount_paid).toFixed(2)})`);

  const { data: payment, error: perr } = await supabase.from('payments').insert({
    bill_id: billId,
    tenant_id: bill.tenant_id,
    amount: input.amount,
    method: input.method,
    paid_at: input.paid_at || new Date().toISOString(),
    reference: input.reference || null,
    notes: input.notes || null,
    recorded_by: recordedBy?.id || null,
  }).select().single();
  if (perr) throw perr;

  const { data: updatedBill, error: berr } = await supabase
    .from('bills').update({ amount_paid: newPaid }).eq('id', billId).select(BILL_SELECT).single();
  if (berr) throw berr;

  return { bill: updatedBill, payment };
};

const listPayments = async ({ tenant_id, bill_id, page = 1, page_size = 100 }, currentUser) => {
  let query = supabase.from('payments')
    .select('id, bill_id, tenant_id, amount, method, paid_at, reference, notes, created_at, recorded_by, bill:bills(id, category, billing_month, amount)', { count: 'exact' })
    .order('paid_at', { ascending: false });

  if (currentUser?.role === 'tenant') {
    const { data: t } = await supabase.from('tenants').select('id').eq('user_id', currentUser.id).maybeSingle();
    if (!t) return { rows: [], total: 0, page, page_size };
    query = query.eq('tenant_id', t.id);
  } else if (tenant_id) {
    query = query.eq('tenant_id', tenant_id);
  }
  if (bill_id) query = query.eq('bill_id', bill_id);

  const from = (page - 1) * page_size;
  query = query.range(from, from + page_size - 1);

  const { data, error, count } = await query;
  if (error) throw error;
  return { rows: data || [], total: count ?? 0, page, page_size };
};

const bulkGenerate = async (input) => {
  const billing_month = normalizeMonth(input.billing_month);
  const dueDate = `${input.billing_month}-${String(input.due_day).padStart(2, '0')}`;

  // Fetch tenants with their per-tenant + room rent so we can default the amount
  let baseQuery = supabase
    .from('tenants')
    .select('id, monthly_rent, room:rooms(monthly_rent)')
    .eq('status', 'active');
  if (!input.generate_for_all_active && input.tenant_ids && input.tenant_ids.length > 0) {
    baseQuery = supabase
      .from('tenants')
      .select('id, monthly_rent, room:rooms(monthly_rent)')
      .in('id', input.tenant_ids);
  }
  const { data: tenants } = await baseQuery;
  if (!tenants || tenants.length === 0) return { created: 0, skipped: 0, created_ids: [] };

  const created = [];
  const skipped = [];
  for (const t of tenants) {
    const amount = input.amount != null
      ? input.amount
      : Number(t.monthly_rent ?? t.room?.monthly_rent ?? 0);
    if (amount <= 0) { skipped.push(t.id); continue; }
    if (input.skip_if_exists) {
      const { data: existing } = await supabase.from('bills')
        .select('id').eq('tenant_id', t.id).eq('category', input.category).eq('billing_month', billing_month).maybeSingle();
      if (existing) { skipped.push(t.id); continue; }
    }
    const { data, error } = await supabase.from('bills').insert({
      tenant_id: t.id,
      category: input.category,
      amount,
      billing_month,
      due_date: dueDate,
      description: input.description || null,
    }).select('id').single();
    if (error) { skipped.push(t.id); continue; }
    created.push(data.id);
  }
  return { created: created.length, skipped: skipped.length, created_ids: created };
};

const summary = async (currentUser) => {
  const isTenant = currentUser?.role === 'tenant';
  let tenantId = null;
  if (isTenant) {
    const { data: t } = await supabase.from('tenants').select('id').eq('user_id', currentUser.id).maybeSingle();
    if (!t) return { total_due: 0, overdue: 0, paid_this_month: 0 };
    tenantId = t.id;
  }

  let base = supabase.from('bills').select('amount, amount_paid, status, billing_month');
  if (tenantId) base = base.eq('tenant_id', tenantId);

  const { data: bills } = await base;
  let total_due = 0;
  let overdue = 0;
  let paid_this_month = 0;
  const monthStr = new Date().toISOString().slice(0, 7) + '-01';
  for (const b of bills || []) {
    const remaining = Number(b.amount) - Number(b.amount_paid);
    if (b.status !== 'paid') total_due += remaining;
    if (b.status === 'overdue') overdue += remaining;
    if (b.billing_month === monthStr) paid_this_month += Number(b.amount_paid);
  }
  return { total_due, overdue, paid_this_month, count_bills: bills?.length || 0 };
};

/**
 * Member-list summary for a given month: every active member with their
 * rent bill status. Powers the simple "Bills" tab on the admin app.
 */
const membersSummary = async ({ billing_month, category = 'rent' }) => {
  const monthDate = `${billing_month}-01`;
  const { data: tenants } = await supabase
    .from('tenants')
    .select(`
      id, monthly_rent,
      user:users!tenants_user_id_fkey ( id, full_name, user_code, email, phone ),
      room:rooms ( id, room_number ),
      bed:beds ( id, bed_label )
    `)
    .eq('status', 'active')
    .order('created_at', { ascending: false });

  const tenantIds = (tenants || []).map((t) => t.id);
  const billsByTenant = new Map();
  if (tenantIds.length > 0) {
    const { data: bills } = await supabase
      .from('bills')
      .select('id, tenant_id, amount, amount_paid, status')
      .eq('category', category)
      .eq('billing_month', monthDate)
      .in('tenant_id', tenantIds);
    for (const b of bills || []) billsByTenant.set(b.tenant_id, b);
  }

  return (tenants || []).map((t) => {
    const bill = billsByTenant.get(t.id) || null;
    const monthlyRent = Number(t.monthly_rent ?? 0);
    const defaultAmount = category === 'rent' ? monthlyRent : 0;
    return {
      tenant_id: t.id,
      user: t.user || null,
      room: t.room || null,
      bed: t.bed || null,
      monthly_rent: monthlyRent,
      bill,
      status: bill?.status ?? 'unbilled',
      amount: Number(bill?.amount ?? defaultAmount),
      amount_paid: Number(bill?.amount_paid ?? 0),
    };
  });
};

/**
 * Admin toggle: mark a member's rent for the month as paid / partial / unpaid.
 * Creates the bill if it doesn't exist yet; never errors on idempotent calls.
 */
/**
 * Each admin click also writes a row to `payments` so the member can see every
 * individual payment event in their My Bills view.
 *
 *   - "paid":    if any due remains, creates a payment for (amount - amount_paid)
 *                and bumps amount_paid up to amount.
 *   - "partial": paid_amount is the *increment* for this click. A new payment
 *                row is inserted and amount_paid is bumped by that much
 *                (capped at the bill total).
 *   - "unpaid":  deletes every payment row for the bill and resets amount_paid.
 */
const setStatus = async ({ tenant_id, billing_month, status, category = 'rent', amount, paid_amount }, recordedBy) => {
  const monthDate = `${billing_month}-01`;

  const { data: tenant } = await supabase
    .from('tenants').select('id, monthly_rent, room:rooms(monthly_rent)')
    .eq('id', tenant_id).maybeSingle();
  if (!tenant) throw NotFound('Member not found');

  // Default amount for new bills:
  //   - rent: tenant's monthly_rent (or room's)
  //   - other categories: must be set on the bill already (electricity bills
  //     are created by /electricity); if there's no existing bill we can't
  //     guess an amount, so refuse.
  const rentDefault = Number(amount ?? tenant.monthly_rent ?? tenant.room?.monthly_rent ?? 0);

  const { data: existing } = await supabase
    .from('bills').select('*')
    .eq('tenant_id', tenant_id).eq('billing_month', monthDate).eq('category', category).maybeSingle();

  const dueDate = `${billing_month}-10`;
  let billId = existing?.id;
  let billAmount = existing?.amount != null ? Number(existing.amount) : rentDefault;
  let currentPaid = existing?.amount_paid != null ? Number(existing.amount_paid) : 0;

  if (!existing) {
    if (category !== 'rent' && rentDefault <= 0) {
      throw BadRequest(`No ${category} bill exists for this member and month. Generate one first.`);
    }
    const desc = category === 'rent' ? 'Monthly rent' : `${category} bill`;
    const { data: inserted, error } = await supabase.from('bills').insert({
      tenant_id, category, amount: rentDefault,
      billing_month: monthDate, due_date: dueDate, description: desc,
    }).select('id, amount, amount_paid').single();
    if (error) throw error;
    billId = inserted.id;
    billAmount = Number(inserted.amount);
    currentPaid = Number(inserted.amount_paid);
  }

  let newAmountPaid = currentPaid;
  const nowIso = new Date().toISOString();

  if (status === 'paid') {
    const remaining = billAmount - currentPaid;
    if (remaining > 0.001) {
      await supabase.from('payments').insert({
        bill_id: billId, tenant_id, amount: remaining,
        method: 'cash', paid_at: nowIso,
        notes: 'Marked fully paid by admin',
        recorded_by: recordedBy?.id || null,
      });
      newAmountPaid = billAmount;
    }
  } else if (status === 'partial') {
    const inc = Number(paid_amount ?? 0);
    if (inc <= 0) throw BadRequest('Partial amount must be greater than 0');
    const cappedInc = Math.min(inc, billAmount - currentPaid);
    if (cappedInc <= 0) throw BadRequest('Bill is already fully paid');
    await supabase.from('payments').insert({
      bill_id: billId, tenant_id, amount: cappedInc,
      method: 'cash', paid_at: nowIso,
      notes: 'Partial payment recorded by admin',
      recorded_by: recordedBy?.id || null,
    });
    newAmountPaid = currentPaid + cappedInc;
  } else if (status === 'unpaid') {
    await supabase.from('payments').delete().eq('bill_id', billId);
    newAmountPaid = 0;
  }

  const { data: updated, error: uerr } = await supabase
    .from('bills')
    .update({ amount_paid: newAmountPaid })
    .eq('id', billId)
    .select('id, tenant_id, amount, amount_paid, status, billing_month')
    .single();
  if (uerr) throw uerr;
  return updated;
};

module.exports = {
  create, list, getById, update, remove,
  recordPayment, listPayments, bulkGenerate, summary,
  membersSummary, setStatus,
};
