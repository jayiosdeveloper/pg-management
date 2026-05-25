const Joi = require('joi');

const bedSchema = Joi.object({
  bed_label: Joi.string().trim().min(1).max(20).required(),
});

const createRoomSchema = Joi.object({
  room_number: Joi.string().trim().min(1).max(40).required(),
  floor: Joi.number().integer().min(-2).max(50).allow(null),
  capacity: Joi.number().integer().min(1).max(20).default(1),
  monthly_rent: Joi.number().min(0).default(0),
  description: Joi.string().trim().max(500).allow(null, ''),
  // Either an array of bed labels, or beds auto-generated A, B, ...
  beds: Joi.array().items(bedSchema).optional(),
});

const updateRoomSchema = Joi.object({
  room_number: Joi.string().trim().min(1).max(40).optional(),
  floor: Joi.number().integer().min(-2).max(50).allow(null),
  capacity: Joi.number().integer().min(1).max(20).optional(),
  monthly_rent: Joi.number().min(0).optional(),
  description: Joi.string().trim().max(500).allow(null, ''),
});

const listRoomsSchema = Joi.object({
  status: Joi.string().valid('all', 'vacant', 'partial', 'occupied').default('all'),
  q: Joi.string().trim().allow('').default(''),
});

const addBedSchema = bedSchema;

module.exports = { createRoomSchema, updateRoomSchema, listRoomsSchema, addBedSchema };
