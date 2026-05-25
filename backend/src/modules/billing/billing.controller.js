const asyncHandler = require('../../utils/asyncHandler');
const { ok, created } = require('../../utils/response');
const service = require('./billing.service');

const list = asyncHandler(async (req, res) => {
  const result = await service.list(req.query, req.user);
  return ok(res, result.rows, 'OK', { page: result.page, page_size: result.page_size, total: result.total });
});

const get = asyncHandler(async (req, res) => {
  const bill = await service.getById(req.params.id, req.user);
  return ok(res, bill);
});

const createBill = asyncHandler(async (req, res) => {
  const bill = await service.create(req.body);
  return created(res, bill, 'Bill created');
});

const updateBill = asyncHandler(async (req, res) => {
  const bill = await service.update(req.params.id, req.body);
  return ok(res, bill, 'Bill updated');
});

const removeBill = asyncHandler(async (req, res) => {
  await service.remove(req.params.id);
  return ok(res, null, 'Bill deleted');
});

const recordPayment = asyncHandler(async (req, res) => {
  const result = await service.recordPayment(req.params.id, req.body, req.user);
  return created(res, result, 'Payment recorded');
});

const listPayments = asyncHandler(async (req, res) => {
  const result = await service.listPayments(req.query, req.user);
  return ok(res, result.rows, 'OK', { page: result.page, page_size: result.page_size, total: result.total });
});

const bulkGenerate = asyncHandler(async (req, res) => {
  const result = await service.bulkGenerate(req.body);
  return created(res, result, 'Bills generated');
});

const summary = asyncHandler(async (req, res) => {
  const data = await service.summary(req.user);
  return ok(res, data);
});

module.exports = { list, get, createBill, updateBill, removeBill, recordPayment, listPayments, bulkGenerate, summary };
