const router = require('express').Router();
const validate = require('../../middleware/validate');
const { requireAuth, requireRole } = require('../../middleware/auth');
const { imageUpload } = require('../../middleware/upload');
const v = require('./tenant.validators');
const c = require('./tenant.controller');

// All tenant endpoints require admin
router.use(requireAuth, requireRole('admin'));

router.get('/', validate(v.listTenantsSchema, 'query'), c.list);
router.post('/', validate(v.createTenantSchema), c.createTenant);

router.get('/:id', c.get);
router.patch('/:id', validate(v.updateTenantSchema), c.updateTenant);
router.delete('/:id', c.removeTenant);

router.post('/:id/photo', imageUpload.single('file'), c.uploadPhoto);
router.post('/:id/aadhaar-front', imageUpload.single('file'), c.uploadAadhaarFront);
router.post('/:id/aadhaar-back', imageUpload.single('file'), c.uploadAadhaarBack);

// Credentials view + reset password
router.get('/:id/credentials', c.getCredentials);
router.post('/:id/reset-password', c.resetMemberPassword);

module.exports = router;
