const Joi = require('joi');

const dateOrNull = Joi.alternatives().try(Joi.date().iso(), Joi.valid(null, ''));

const createTenantSchema = Joi.object({
  full_name: Joi.string().trim().min(2).max(120).required(),
  email: Joi.string().email().trim().lowercase().allow(null, ''),
  phone: Joi.string().trim().max(20).allow(null, ''),

  date_of_birth: dateOrNull,
  gender: Joi.string().trim().max(20).allow(null, ''),
  address: Joi.string().trim().max(500).allow(null, ''),
  city: Joi.string().trim().max(80).allow(null, ''),
  state: Joi.string().trim().max(80).allow(null, ''),

  emergency_contact_name: Joi.string().trim().max(120).allow(null, ''),
  emergency_contact_phone: Joi.string().trim().max(20).allow(null, ''),

  occupation: Joi.string().trim().max(120).allow(null, ''),
  id_proof_type: Joi.string().trim().max(40).allow(null, ''),
  id_proof_number: Joi.string().trim().max(80).allow(null, ''),

  room_id: Joi.string().uuid().allow(null, ''),
  bed_id: Joi.string().uuid().allow(null, ''),

  joining_date: Joi.date().iso().required(),
  leaving_date: dateOrNull,

  monthly_rent: Joi.number().min(0).allow(null),
  security_deposit: Joi.number().min(0).default(0),

  notes: Joi.string().trim().max(1000).allow(null, ''),
});

const updateTenantSchema = createTenantSchema.fork(
  ['full_name', 'joining_date'],
  (s) => s.optional(),
).keys({
  status: Joi.string().valid('active', 'inactive', 'left').optional(),
});

const listTenantsSchema = Joi.object({
  q: Joi.string().trim().allow('').default(''),
  status: Joi.string().valid('active', 'inactive', 'left', 'all').default('active'),
  room_id: Joi.string().uuid().optional(),
  page: Joi.number().integer().min(1).default(1),
  page_size: Joi.number().integer().min(1).max(100).default(20),
});

module.exports = { createTenantSchema, updateTenantSchema, listTenantsSchema };
