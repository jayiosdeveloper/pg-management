const router = require('express').Router();
const validate = require('../../middleware/validate');
const { requireAuth, requireRole } = require('../../middleware/auth');
const { imageUpload } = require('../../middleware/upload');
const v = require('./worker.validators');
const c = require('./worker.controller');

router.use(requireAuth, requireRole('admin'));

router.get('/', validate(v.listWorkersSchema, 'query'), c.list);
router.post('/', validate(v.createWorkerSchema), c.createWorker);
router.get('/salary-payments', c.listSalaryPayments);

router.get('/:id', c.get);
router.patch('/:id', validate(v.updateWorkerSchema), c.updateWorker);
router.delete('/:id', c.removeWorker);

router.post('/:id/photo', imageUpload.single('file'), c.uploadPhoto);
router.post('/:id/aadhaar-front', imageUpload.single('file'), c.uploadAadhaarFront);
router.post('/:id/aadhaar-back', imageUpload.single('file'), c.uploadAadhaarBack);

router.get('/:id/credentials', c.getCredentials);
router.post('/:id/reset-password', c.resetWorkerPassword);

router.post('/:id/salary', validate(v.recordSalarySchema), c.recordSalary);

module.exports = router;
