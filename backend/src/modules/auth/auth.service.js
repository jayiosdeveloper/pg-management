const bcrypt = require('bcryptjs');
const crypto = require('crypto');
const supabase = require('../../lib/supabase');
const { signAccess, signRefresh, verifyRefresh } = require('../../utils/jwt');
const { Unauthorized, NotFound, BadRequest } = require('../../utils/errors');
const env = require('../../config/env');

const hashPassword = (raw) => bcrypt.hash(raw, 10);
const comparePassword = (raw, hash) => bcrypt.compare(raw, hash);
const sha256 = (s) => crypto.createHash('sha256').update(s).digest('hex');

const findUserByIdentifier = async (identifier) => {
  // Try email first, then user_code
  const trimmed = identifier.trim();
  let user = null;

  if (trimmed.includes('@')) {
    const { data } = await supabase.from('users').select('*').eq('email', trimmed).maybeSingle();
    user = data;
  } else {
    const { data } = await supabase
      .from('users')
      .select('*')
      .eq('user_code', trimmed.toUpperCase())
      .maybeSingle();
    user = data;
  }
  return user;
};

const issueTokens = async (user) => {
  const access = signAccess({ sub: user.id, role: user.role });
  const refresh = signRefresh({ sub: user.id, role: user.role });

  // Store refresh hash for revocation tracking
  const decoded = verifyRefresh(refresh);
  await supabase.from('refresh_tokens').insert({
    user_id: user.id,
    token_hash: sha256(refresh),
    expires_at: new Date(decoded.exp * 1000).toISOString(),
  });

  return { access_token: access, refresh_token: refresh };
};

const login = async ({ identifier, password, fcm_token }) => {
  const user = await findUserByIdentifier(identifier);
  if (!user) throw Unauthorized('Invalid credentials');
  if (!user.is_active) throw Unauthorized('Account is disabled');

  const ok = await comparePassword(password, user.password_hash);
  if (!ok) throw Unauthorized('Invalid credentials');

  const updates = { last_login_at: new Date().toISOString() };
  if (fcm_token) updates.fcm_token = fcm_token;
  await supabase.from('users').update(updates).eq('id', user.id);

  const tokens = await issueTokens(user);

  return {
    user: {
      id: user.id,
      user_code: user.user_code,
      full_name: user.full_name,
      email: user.email,
      role: user.role,
    },
    ...tokens,
  };
};

const refresh = async ({ refresh_token }) => {
  let decoded;
  try {
    decoded = verifyRefresh(refresh_token);
  } catch (e) {
    throw Unauthorized('Invalid refresh token');
  }
  const hash = sha256(refresh_token);
  const { data: tokenRow } = await supabase
    .from('refresh_tokens')
    .select('*')
    .eq('token_hash', hash)
    .maybeSingle();
  if (!tokenRow || tokenRow.revoked_at) throw Unauthorized('Refresh token revoked');

  // Rotate: revoke old, issue new
  await supabase.from('refresh_tokens').update({ revoked_at: new Date().toISOString() }).eq('id', tokenRow.id);

  const { data: user } = await supabase.from('users').select('*').eq('id', decoded.sub).single();
  if (!user || !user.is_active) throw Unauthorized('User not active');
  return await issueTokens(user);
};

const logout = async (userId, refresh_token) => {
  if (refresh_token) {
    const hash = sha256(refresh_token);
    await supabase.from('refresh_tokens').update({ revoked_at: new Date().toISOString() }).eq('token_hash', hash);
  } else {
    await supabase.from('refresh_tokens').update({ revoked_at: new Date().toISOString() })
      .eq('user_id', userId)
      .is('revoked_at', null);
  }
};

const changePassword = async (userId, current_password, new_password) => {
  const { data: user } = await supabase.from('users').select('password_hash').eq('id', userId).single();
  if (!user) throw NotFound('User not found');
  const ok = await comparePassword(current_password, user.password_hash);
  if (!ok) throw BadRequest('Current password is incorrect');
  const password_hash = await hashPassword(new_password);
  await supabase.from('users').update({ password_hash }).eq('id', userId);
  // Revoke existing refresh tokens for safety
  await supabase.from('refresh_tokens').update({ revoked_at: new Date().toISOString() })
    .eq('user_id', userId).is('revoked_at', null);
};

// Admin-only: reset password for any user
const resetPassword = async (user_id, new_password) => {
  const password_hash = await hashPassword(new_password);
  const { data, error } = await supabase
    .from('users')
    .update({ password_hash })
    .eq('id', user_id)
    .select('id')
    .single();
  if (error || !data) throw NotFound('User not found');
  await supabase.from('refresh_tokens').update({ revoked_at: new Date().toISOString() })
    .eq('user_id', user_id).is('revoked_at', null);
};

const me = async (userId) => {
  const { data: user } = await supabase
    .from('users')
    .select('id, user_code, full_name, email, phone, role, is_active, last_login_at, created_at')
    .eq('id', userId)
    .single();
  if (!user) throw NotFound('User not found');

  if (user.role === 'tenant') {
    const { data: tenant } = await supabase
      .from('tenants')
      .select('*, room:rooms(*), bed:beds(*)')
      .eq('user_id', user.id)
      .maybeSingle();
    user.tenant = tenant || null;
  }
  return user;
};

module.exports = {
  hashPassword,
  comparePassword,
  login,
  refresh,
  logout,
  changePassword,
  resetPassword,
  me,
};
