const router = require('express').Router();
const validate = require('../../middleware/validate');
const { requireAuth, requireRole } = require('../../middleware/auth');
const { loginLimiter } = require('../../middleware/rateLimit');
const v = require('./auth.validators');
const c = require('./auth.controller');

router.post('/login', loginLimiter, validate(v.loginSchema), c.login);
router.post('/refresh', validate(v.refreshSchema), c.refresh);
router.post('/logout', requireAuth, c.logout);
router.get('/me', requireAuth, c.me);
router.post('/change-password', requireAuth, validate(v.changePasswordSchema), c.changePassword);
router.post('/reset-password', requireAuth, requireRole('admin'), validate(v.resetPasswordSchema), c.resetPassword);

module.exports = router;
