const supabase = require('../../lib/supabase');
const { NotFound } = require('../../utils/errors');

const list = async ({ unread_only = false, page = 1, page_size = 50 }, currentUser) => {
  let q = supabase.from('notifications').select('*', { count: 'exact' }).order('sent_at', { ascending: false });
  if (currentUser.role === 'tenant') {
    q = q.or(`user_id.eq.${currentUser.id},user_id.is.null`);
  }
  if (unread_only === true || unread_only === 'true') q = q.eq('is_read', false);
  const from = (page - 1) * page_size;
  q = q.range(from, from + page_size - 1);
  const { data, error, count } = await q;
  if (error) throw error;
  return { rows: data || [], total: count ?? 0, page, page_size };
};

const send = async (input, sender) => {
  // input: { user_id?, all_tenants?, title, body, type, data }
  let recipients = [];
  if (input.all_tenants) {
    const { data: users } = await supabase.from('users').select('id').eq('role', 'tenant').eq('is_active', true);
    recipients = (users || []).map((u) => u.id);
  } else if (input.user_id) {
    recipients = [input.user_id];
  } else {
    // null user_id = broadcast (any tenant sees it in their feed)
    recipients = [null];
  }

  const rows = recipients.map((uid) => ({
    user_id: uid,
    title: input.title,
    body: input.body,
    type: input.type || 'announcement',
    data: input.data || {},
  }));
  const { error } = await supabase.from('notifications').insert(rows);
  if (error) throw error;
  return { delivered: recipients.length };
};

const markRead = async (id, currentUser) => {
  let q = supabase.from('notifications').update({ is_read: true, read_at: new Date().toISOString() }).eq('id', id);
  if (currentUser.role === 'tenant') q = q.or(`user_id.eq.${currentUser.id},user_id.is.null`);
  const { error, data } = await q.select('id').maybeSingle();
  if (error) throw error;
  if (!data) throw NotFound('Notification not found');
  return data;
};

const markAllRead = async (currentUser) => {
  let q = supabase.from('notifications').update({ is_read: true, read_at: new Date().toISOString() }).eq('is_read', false);
  if (currentUser.role === 'tenant') q = q.or(`user_id.eq.${currentUser.id},user_id.is.null`);
  const { error } = await q;
  if (error) throw error;
};

const registerFcmToken = async (userId, token) => {
  await supabase.from('users').update({ fcm_token: token }).eq('id', userId);
};

module.exports = { list, send, markRead, markAllRead, registerFcmToken };
