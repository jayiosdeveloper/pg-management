const supabase = require('../../lib/supabase');
const { NotFound, BadRequest, Conflict } = require('../../utils/errors');

const READING_SELECT = `
  id, room_id, billing_month, start_reading, end_reading, rate_per_unit,
  units_used, total_amount, per_member_amount, member_count_at_creation,
  notes, created_by, created_at,
  room:rooms ( id, room_number, floor )
`;

const create = async (input, createdBy) => {
  const monthDate = `${input.billing_month}-01`;
  if (input.end_reading < input.start_reading) {
    throw BadRequest('End reading must be greater than or equal to start reading');
  }

  // Reject duplicates — admin can delete the old one if they want to re-enter
  const { data: existing } = await supabase
    .from('electricity_readings').select('id')
    .eq('room_id', input.room_id).eq('billing_month', monthDate).maybeSingle();
  if (existing) throw Conflict('Electricity reading already exists for this room and month');

  // Verify room + collect its active members
  const { data: room } = await supabase
    .from('rooms').select('id, room_number, monthly_rent').eq('id', input.room_id).maybeSingle();
  if (!room) throw NotFound('Room not found');

  const { data: tenants } = await supabase
    .from('tenants').select('id').eq('room_id', input.room_id).eq('status', 'active');
  const memberIds = (tenants || []).map((t) => t.id);
  if (memberIds.length === 0) throw BadRequest('No active members in this room to bill');

  const units = Number(input.end_reading) - Number(input.start_reading);
  const total = Math.round(units * Number(input.rate_per_unit) * 100) / 100;
  const perMember = Math.round((total / memberIds.length) * 100) / 100;

  // Save reading
  const { data: reading, error: rerr } = await supabase.from('electricity_readings').insert({
    room_id: input.room_id,
    billing_month: monthDate,
    start_reading: input.start_reading,
    end_reading: input.end_reading,
    rate_per_unit: input.rate_per_unit,
    total_amount: total,
    per_member_amount: perMember,
    member_count_at_creation: memberIds.length,
    notes: input.notes || null,
    created_by: createdBy?.id || null,
  }).select(READING_SELECT).single();
  if (rerr) throw rerr;

  // Create / replace electricity bills for each member in this room for this month
  const dueDate = `${input.billing_month}-${String(input.due_day || 10).padStart(2, '0')}`;
  const desc = `Electricity ${input.billing_month}: ${units.toFixed(2)} units × ₹${input.rate_per_unit} / ${memberIds.length} members`;

  const billsCreated = [];
  for (const tid of memberIds) {
    // Replace any existing zero-paid electricity bill for the same month; otherwise insert
    const { data: existingBill } = await supabase.from('bills')
      .select('id, amount_paid').eq('tenant_id', tid)
      .eq('category', 'electricity').eq('billing_month', monthDate).maybeSingle();

    if (existingBill && Number(existingBill.amount_paid) === 0) {
      // Safe to overwrite a zero-paid bill
      const { error } = await supabase.from('bills').update({
        amount: perMember, description: desc, due_date: dueDate,
      }).eq('id', existingBill.id);
      if (!error) billsCreated.push(existingBill.id);
    } else if (!existingBill) {
      const { data: ins } = await supabase.from('bills').insert({
        tenant_id: tid, category: 'electricity', amount: perMember,
        billing_month: monthDate, due_date: dueDate, description: desc,
      }).select('id').single();
      if (ins) billsCreated.push(ins.id);
    }
    // else: existing bill has payments — leave it alone
  }

  return { reading, bills_created: billsCreated.length };
};

const list = async ({ billing_month }) => {
  const monthDate = `${billing_month}-01`;
  const { data, error } = await supabase
    .from('electricity_readings')
    .select(READING_SELECT)
    .eq('billing_month', monthDate)
    .order('created_at', { ascending: false });
  if (error) throw error;
  return data || [];
};

const remove = async (id) => {
  const { data: r } = await supabase.from('electricity_readings').select('room_id, billing_month').eq('id', id).maybeSingle();
  if (!r) throw NotFound('Reading not found');

  // Delete the associated bills (only zero-paid ones, so we never blow away a
  // payment record by accident).
  const { data: members } = await supabase.from('tenants').select('id').eq('room_id', r.room_id);
  const ids = (members || []).map((t) => t.id);
  if (ids.length > 0) {
    await supabase.from('bills').delete()
      .eq('category', 'electricity').eq('billing_month', r.billing_month)
      .in('tenant_id', ids).eq('amount_paid', 0);
  }
  const { error } = await supabase.from('electricity_readings').delete().eq('id', id);
  if (error) throw error;
  return { id };
};

module.exports = { create, list, remove };
