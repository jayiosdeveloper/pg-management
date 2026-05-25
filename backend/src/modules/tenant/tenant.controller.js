const asyncHandler = require('../../utils/asyncHandler');
const { ok, created } = require('../../utils/response');
const service = require('./tenant.service');

const list = asyncHandler(async (req, res) => {
  const result = await service.list(req.query);
  return ok(res, result.rows, 'OK', {
    page: result.page,
    page_size: result.page_size,
    total: result.total,
  });
});

const get = asyncHandler(async (req, res) => {
  const tenant = await service.getById(req.params.id);
  return ok(res, tenant);
});

const createTenant = asyncHandler(async (req, res) => {
  const result = await service.create(req.body);
  return created(res, result, 'Tenant created');
});

const updateTenant = asyncHandler(async (req, res) => {
  const tenant = await service.update(req.params.id, req.body);
  return ok(res, tenant, 'Tenant updated');
});

const removeTenant = asyncHandler(async (req, res) => {
  await service.remove(req.params.id);
  return ok(res, null, 'Tenant deleted');
});

const uploadPhoto = asyncHandler(async (req, res) => {
  const result = await service.uploadDocument(req.params.id, 'photo_url', req.file);
  return ok(res, result, 'Photo uploaded');
});

const uploadAadhaarFront = asyncHandler(async (req, res) => {
  const result = await service.uploadDocument(req.params.id, 'aadhaar_front_url', req.file);
  return ok(res, result, 'Aadhaar (front) uploaded');
});

const uploadAadhaarBack = asyncHandler(async (req, res) => {
  const result = await service.uploadDocument(req.params.id, 'aadhaar_back_url', req.file);
  return ok(res, result, 'Aadhaar (back) uploaded');
});

const getCredentials = asyncHandler(async (req, res) => {
  const data = await service.getCredentials(req.params.id);
  return ok(res, data);
});

const resetMemberPassword = asyncHandler(async (req, res) => {
  const result = await service.resetMemberPassword(req.params.id, req.body?.new_password);
  return ok(res, result, 'Password reset');
});

module.exports = {
  list, get, createTenant, updateTenant, removeTenant,
  uploadPhoto, uploadAadhaarFront, uploadAadhaarBack,
  getCredentials, resetMemberPassword,
};
