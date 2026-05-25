const asyncHandler = require('../../utils/asyncHandler');
const { ok, created } = require('../../utils/response');
const service = require('./auth.service');

const login = asyncHandler(async (req, res) => {
  const result = await service.login(req.body);
  return ok(res, result, 'Login successful');
});

const refresh = asyncHandler(async (req, res) => {
  const tokens = await service.refresh(req.body);
  return ok(res, tokens, 'Token refreshed');
});

const logout = asyncHandler(async (req, res) => {
  await service.logout(req.user.id, req.body?.refresh_token);
  return ok(res, null, 'Logged out');
});

const me = asyncHandler(async (req, res) => {
  const data = await service.me(req.user.id);
  return ok(res, data);
});

const changePassword = asyncHandler(async (req, res) => {
  const { current_password, new_password } = req.body;
  await service.changePassword(req.user.id, current_password, new_password);
  return ok(res, null, 'Password changed');
});

const resetPassword = asyncHandler(async (req, res) => {
  await service.resetPassword(req.body.user_id, req.body.new_password);
  return ok(res, null, 'Password reset');
});

module.exports = { login, refresh, logout, me, changePassword, resetPassword };
