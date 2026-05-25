const router = require('express').Router();
const validate = require('../../middleware/validate');
const { requireAuth, requireRole } = require('../../middleware/auth');
const v = require('./billing.validators');
const c = require('./billing.controller');

router.use(requireAuth);

router.get('/summary', c.summary);
router.get('/payments', c.listPayments);
router.get('/members-summary', requireRole('admin'), validate(v.membersSummarySchema, 'query'), c.membersSummary);
router.get('/', validate(v.listBillsSchema, 'query'), c.list);
router.get('/:id', c.get);

// Admin-only mutations
router.post('/set-status', requireRole('admin'), validate(v.setStatusSchema), c.setStatus);
router.post('/bulk-generate', requireRole('admin'), validate(v.bulkGenerateSchema), c.bulkGenerate);
router.post('/', requireRole('admin'), validate(v.createBillSchema), c.createBill);
router.patch('/:id', requireRole('admin'), validate(v.updateBillSchema), c.updateBill);
router.delete('/:id', requireRole('admin'), c.removeBill);
router.post('/:id/payments', requireRole('admin'), validate(v.recordPaymentSchema), c.recordPayment);

module.exports = router;
