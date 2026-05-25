const asyncHandler = require('../../utils/asyncHandler');
const { ok, created } = require('../../utils/response');
const service = require('./room.service');

const list = asyncHandler(async (req, res) => {
  const rows = await service.list(req.query);
  return ok(res, rows);
});

const get = asyncHandler(async (req, res) => {
  const room = await service.getById(req.params.id);
  return ok(res, room);
});

const createRoom = asyncHandler(async (req, res) => {
  const room = await service.create(req.body);
  return created(res, room, 'Room created');
});

const updateRoom = asyncHandler(async (req, res) => {
  const room = await service.update(req.params.id, req.body);
  return ok(res, room, 'Room updated');
});

const removeRoom = asyncHandler(async (req, res) => {
  await service.remove(req.params.id);
  return ok(res, null, 'Room deleted');
});

const addBed = asyncHandler(async (req, res) => {
  const room = await service.addBed(req.params.id, req.body);
  return created(res, room, 'Bed added');
});

const removeBed = asyncHandler(async (req, res) => {
  await service.removeBed(req.params.bedId);
  return ok(res, null, 'Bed removed');
});

module.exports = { list, get, createRoom, updateRoom, removeRoom, addBed, removeBed };
