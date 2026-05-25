const router = require('express').Router();
const authRoutes = require('../modules/auth/auth.routes');

router.get('/health', (req, res) => {
  res.json({ success: true, message: 'PG Management API is alive', uptime: process.uptime() });
});

router.use('/auth', authRoutes);
router.use('/tenants', require('../modules/tenant/tenant.routes'));
router.use('/workers', require('../modules/worker/worker.routes'));
router.use('/rooms', require('../modules/room/room.routes'));
router.use('/bills', require('../modules/billing/billing.routes'));
router.use('/invoices', require('../modules/pdf/pdf.routes'));
router.use('/notifications', require('../modules/notification/notification.routes'));
router.use('/complaints', require('../modules/complaint/complaint.routes'));
router.use('/visitors', require('../modules/visitor/visitor.routes'));
router.use('/analytics', require('../modules/analytics/analytics.routes'));

// Future modules will be mounted here
// router.use('/bills', require('../modules/billing/billing.routes'));
// router.use('/payments', require('../modules/payment/payment.routes'));
// router.use('/notifications', require('../modules/notification/notification.routes'));
// router.use('/complaints', require('../modules/complaint/complaint.routes'));
// router.use('/visitors', require('../modules/visitor/visitor.routes'));
// router.use('/analytics', require('../modules/analytics/analytics.routes'));
// router.use('/upload', require('../modules/upload/upload.routes'));
// router.use('/invoices', require('../modules/pdf/pdf.routes'));

module.exports = router;
