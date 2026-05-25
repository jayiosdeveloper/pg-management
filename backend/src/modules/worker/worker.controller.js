const asyncHandler = require('../../utils/asyncHandler');
const { ok, created } = require('../../utils/response');
const service = require('./worker.service');

const list = asyncHandler(async (req, res) => ok(res, await service.list(req.query)));
const get = asyncHandler(async (req, res) => ok(res, await service.getById(req.params.id)));
const createWorker = asyncHandler(async (req, res) => created(res, await service.create(req.body), 'Worker created'));
const updateWorker = asyncHandler(async (req, res) => ok(res, await service.update(req.params.id, req.body), 'Worker updated'));
const removeWorker = asyncHandler(async (req, res) => {
  await service.remove(req.params.id);
  return ok(res, null, 'Worker deleted');
});

const uploadPhoto = asyncHandler(async (req, res) => ok(res, await service.uploadDocument(req.params.id, 'photo_url', req.file), 'Photo uploaded'));
const uploadAadhaarFront = asyncHandler(async (req, res) => ok(res, await service.uploadDocument(req.params.id, 'aadhaar_front_url', req.file), 'Aadhaar (front) uploaded'));
const uploadAadhaarBack = asyncHandler(async (req, res) => ok(res, await service.uploadDocument(req.params.id, 'aadhaar_back_url', req.file), 'Aadhaar (back) uploaded'));

const getCredentials = asyncHandler(async (req, res) => ok(res, await service.getCredentials(req.params.id)));
const resetWorkerPassword = asyncHandler(async (req, res) => ok(res, await service.resetPassword(req.params.id, req.body?.new_password), 'Password reset'));

const recordSalary = asyncHandler(async (req, res) => created(res, await service.recordSalary(req.params.id, req.body, req.user), 'Salary recorded'));
const listSalaryPayments = asyncHandler(async (req, res) => ok(res, await service.listSalaryPayments(req.query)));

module.exports = {
  list, get, createWorker, updateWorker, removeWorker,
  uploadPhoto, uploadAadhaarFront, uploadAadhaarBack,
  getCredentials, resetWorkerPassword,
  recordSalary, listSalaryPayments,
};
