const supabase = require('../../lib/supabase');
const { NotFound, Conflict, BadRequest } = require('../../utils/errors');

const ROOM_SELECT = `
  id, room_number, floor, capacity, monthly_rent, description, status, created_at, updated_at,
  beds:beds ( id, bed_label, status )
`;

const list = async ({ status, q }) => {
  let query = supabase.from('rooms').select(ROOM_SELECT).order('room_number', { ascending: true });
  if (status !== 'all') query = query.eq('status', status);
  const { data, error } = await query;
  if (error) throw error;
  let rows = data || [];
  if (q && q.trim()) {
    const needle = q.trim().toLowerCase();
    rows = rows.filter((r) =>
      (r.room_number || '').toLowerCase().includes(needle) ||
      (r.description || '').toLowerCase().includes(needle),
    );
  }

  // Also fetch tenant counts per room (to surface occupied beds / occupants)
  if (rows.length > 0) {
    const ids = rows.map((r) => r.id);
    const { data: tenants } = await supabase
      .from('tenants')
      .select('id, full_name:users(full_name), user_code:users(user_code), room_id, bed_id')
      .in('room_id', ids)
      .eq('status', 'active');
    const byRoom = new Map();
    (tenants || []).forEach((t) => {
      const arr = byRoom.get(t.room_id) || [];
      arr.push({ id: t.id, bed_id: t.bed_id, user: { full_name: t.full_name?.full_name, user_code: t.user_code?.user_code } });
      byRoom.set(t.room_id, arr);
    });
    rows = rows.map((r) => ({ ...r, tenants: byRoom.get(r.id) || [] }));
  }
  return rows;
};

const getById = async (id) => {
  const { data, error } = await supabase.from('rooms').select(ROOM_SELECT).eq('id', id).maybeSingle();
  if (error) throw error;
  if (!data) throw NotFound('Room not found');
  // Attach active tenants
  const { data: tenants } = await supabase
    .from('tenants')
    .select(`
      id, bed_id,
      user:users!tenants_user_id_fkey ( id, full_name, user_code, email, phone )
    `)
    .eq('room_id', id)
    .eq('status', 'active');
  return { ...data, tenants: tenants || [] };
};

const create = async (input) => {
  const { data: existing } = await supabase
    .from('rooms').select('id').eq('room_number', input.room_number).maybeSingle();
  if (existing) throw Conflict('Room number already exists');

  const { data: room, error } = await supabase
    .from('rooms')
    .insert({
      room_number: input.room_number,
      floor: input.floor ?? null,
      capacity: input.capacity,
      monthly_rent: input.monthly_rent ?? 0,
      description: input.description || null,
    })
    .select()
    .single();
  if (error) throw error;

  // Auto-generate beds if none provided
  const bedRows = input.beds && input.beds.length > 0
    ? input.beds.map((b) => ({ room_id: room.id, bed_label: b.bed_label }))
    : Array.from({ length: input.capacity }, (_, i) => ({
        room_id: room.id,
        bed_label: String.fromCharCode(65 + i), // A, B, C, ...
      }));
  const { error: berr } = await supabase.from('beds').insert(bedRows);
  if (berr) throw berr;

  return await getById(room.id);
};

const update = async (id, input) => {
  await getById(id); // throws if missing

  if (input.room_number) {
    const { data: clash } = await supabase
      .from('rooms').select('id').eq('room_number', input.room_number).neq('id', id).maybeSingle();
    if (clash) throw Conflict('Room number already exists');
  }

  const { error } = await supabase
    .from('rooms')
    .update({
      ...(input.room_number !== undefined ? { room_number: input.room_number } : {}),
      ...(input.floor !== undefined ? { floor: input.floor } : {}),
      ...(input.capacity !== undefined ? { capacity: input.capacity } : {}),
      ...(input.monthly_rent !== undefined ? { monthly_rent: input.monthly_rent } : {}),
      ...(input.description !== undefined ? { description: input.description } : {}),
    })
    .eq('id', id);
  if (error) throw error;
  return await getById(id);
};

const remove = async (id) => {
  // Reject if any active tenant references this room
  const { count } = await supabase
    .from('tenants').select('id', { count: 'exact', head: true })
    .eq('room_id', id).eq('status', 'active');
  if ((count ?? 0) > 0) throw Conflict('Cannot delete room: active tenants still assigned');
  const { error } = await supabase.from('rooms').delete().eq('id', id);
  if (error) throw error;
  return { id };
};

const addBed = async (roomId, { bed_label }) => {
  await getById(roomId);
  const { error } = await supabase.from('beds').insert({ room_id: roomId, bed_label });
  if (error) {
    if (error.code === '23505') throw Conflict('Bed label already exists in this room');
    throw error;
  }
  return await getById(roomId);
};

const removeBed = async (bedId) => {
  const { data: bed } = await supabase.from('beds').select('*').eq('id', bedId).maybeSingle();
  if (!bed) throw NotFound('Bed not found');
  if (bed.status === 'occupied') throw Conflict('Bed is occupied; reassign tenant first');
  const { error } = await supabase.from('beds').delete().eq('id', bedId);
  if (error) throw error;
  return { id: bedId };
};

module.exports = { list, getById, create, update, remove, addBed, removeBed };
