const Joi = require('joi');

const dateOrNull = Joi.alternatives().try(Joi.date().iso(), Joi.valid(null, ''));

const createWorkerSchema = Joi.object({
  full_name: Joi.string().trim().min(2).max(120).required(),
  email: Joi.string().email().trim().lowercase().allow(null, ''),
  phone: Joi.string().trim().max(20).allow(null, ''),
  role_title: Joi.string().trim().max(80).allow(null, ''),
  monthly_salary: Joi.number().min(0).default(0),
  joining_date: Joi.date().iso().required(),
  leaving_date: dateOrNull,

  date_of_birth: dateOrNull,
  gender: Joi.string().trim().max(20).allow(null, ''),
  address: Joi.string().trim().max(500).allow(null, ''),
  city: Joi.string().trim().max(80).allow(null, ''),
  state: Joi.string().trim().max(80).allow(null, ''),
  emergency_contact_name: Joi.string().trim().max(120).allow(null, ''),
  emergency_contact_phone: Joi.string().trim().max(20).allow(null, ''),
  id_proof_type: Joi.string().trim().max(40).allow(null, ''),
  id_proof_number: Joi.string().trim().max(80).allow(null, ''),
  notes: Joi.string().trim().max(1000).allow(null, ''),
});

const updateWorkerSchema = createWorkerSchema.fork(
  ['full_name', 'joining_date'],
  (s) => s.optional(),
).keys({
  status: Joi.string().valid('active', 'inactive', 'left').optional(),
});

const listWorkersSchema = Joi.object({
  q: Joi.string().trim().allow('').default(''),
  status: Joi.string().valid('active', 'inactive', 'left', 'all').default('active'),
});

const recordSalarySchema = Joi.object({
  amount: Joi.number().min(0.01).required(),
  pay_for_month: Joi.string().pattern(/^\d{4}-\d{2}$/).required(),
  method: Joi.string().valid('cash', 'upi', 'bank_transfer', 'card', 'other').default('cash'),
  paid_at: Joi.date().iso().optional(),
  reference: Joi.string().trim().max(120).allow(null, ''),
  notes: Joi.string().trim().max(500).allow(null, ''),
});

module.exports = {
  createWorkerSchema, updateWorkerSchema, listWorkersSchema, recordSalarySchema,
};
