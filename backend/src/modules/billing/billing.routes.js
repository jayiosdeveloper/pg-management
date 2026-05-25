const router = require('express').Router();
const validate = require('../../middleware/validate');
const { requireAuth, requireRole } = require('../../middleware/auth');
const v = require('./billing.validators');
const c = require('./billing.controller');

// Bills - admin can manage; tenants can read their own via list/get
router.use(requireAuth);

router.get('/summary', c.summary);
router.get('/payments', c.listPayments);
router.get('/', validate(v.listBillsSchema, 'query'), c.list);
router.get('/:id', c.get);

// Admin-only mutations from here on
router.post('/bulk-generate', requireRole('admin'), validate(v.bulkGenerateSchema), c.bulkGenerate);
router.post('/', requireRole('admin'), validate(v.createBillSchema), c.createBill);
router.patch('/:id', requireRole('admin'), validate(v.updateBillSchema), c.updateBill);
router.delete('/:id', requireRole('admin'), c.removeBill);
router.post('/:id/payments', requireRole('admin'), validate(v.recordPaymentSchema), c.recordPayment);

module.exports = router;
