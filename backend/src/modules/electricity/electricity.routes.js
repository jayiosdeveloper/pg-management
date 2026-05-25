const router = require('express').Router();
const validate = require('../../middleware/validate');
const { requireAuth, requireRole } = require('../../middleware/auth');
const asyncHandler = require('../../utils/asyncHandler');
const { ok, created } = require('../../utils/response');
const v = require('./electricity.validators');
const service = require('./electricity.service');

router.use(requireAuth, requireRole('admin'));

router.get('/', validate(v.listReadingsSchema, 'query'), asyncHandler(async (req, res) => {
  return ok(res, await service.list(req.query));
}));

router.post('/', validate(v.createReadingSchema), asyncHandler(async (req, res) => {
  const result = await service.create(req.body, req.user);
  return created(res, result, 'Electricity bills generated');
}));

router.delete('/:id', asyncHandler(async (req, res) => {
  await service.remove(req.params.id);
  return ok(res, null, 'Reading deleted');
}));

module.exports = router;
