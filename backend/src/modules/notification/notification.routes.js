const router = require('express').Router();
const Joi = require('joi');
const validate = require('../../middleware/validate');
const { requireAuth, requireRole } = require('../../middleware/auth');
const asyncHandler = require('../../utils/asyncHandler');
const { ok, created } = require('../../utils/response');
const service = require('./notification.service');

const sendSchema = Joi.object({
  user_id: Joi.string().uuid().optional(),
  all_tenants: Joi.boolean().default(false),
  title: Joi.string().trim().min(1).max(120).required(),
  body: Joi.string().trim().min(1).max(2000).required(),
  type: Joi.string().valid('payment_reminder', 'overdue', 'payment_confirmed', 'announcement', 'maintenance', 'other').default('announcement'),
  data: Joi.object().default({}),
}).or('user_id', 'all_tenants');

const tokenSchema = Joi.object({ token: Joi.string().required() });

router.use(requireAuth);

router.get('/', asyncHandler(async (req, res) => {
  const r = await service.list(req.query, req.user);
  return ok(res, r.rows, 'OK', { page: r.page, page_size: r.page_size, total: r.total });
}));

router.post('/mark-all-read', asyncHandler(async (req, res) => {
  await service.markAllRead(req.user);
  return ok(res, null, 'All marked read');
}));

router.post('/:id/read', asyncHandler(async (req, res) => {
  await service.markRead(req.params.id, req.user);
  return ok(res, null, 'Marked read');
}));

router.post('/fcm-token', validate(tokenSchema), asyncHandler(async (req, res) => {
  await service.registerFcmToken(req.user.id, req.body.token);
  return ok(res, null, 'Token saved');
}));

router.post('/', requireRole('admin'), validate(sendSchema), asyncHandler(async (req, res) => {
  const r = await service.send(req.body, req.user);
  return created(res, r, 'Notification sent');
}));

module.exports = router;
