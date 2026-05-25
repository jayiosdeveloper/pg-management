const Joi = require('joi');

const CATEGORIES = ['rent', 'food', 'cleaning', 'repair', 'electricity', 'water', 'other'];

const createBillSchema = Joi.object({
  tenant_id: Joi.string().uuid().required(),
  category: Joi.string().valid(...CATEGORIES).required(),
  amount: Joi.number().min(0).required(),
  billing_month: Joi.string().pattern(/^\d{4}-\d{2}(-01)?$/).required(),
  due_date: Joi.date().iso().required(),
  description: Joi.string().trim().max(500).allow(null, ''),
});

const updateBillSchema = Joi.object({
  amount: Joi.number().min(0).optional(),
  due_date: Joi.date().iso().optional(),
  description: Joi.string().trim().max(500).allow(null, ''),
});

const listBillsSchema = Joi.object({
  tenant_id: Joi.string().uuid().optional(),
  status: Joi.string().valid('all', 'unpaid', 'partial', 'paid', 'overdue').default('all'),
  category: Joi.string().valid(...CATEGORIES, 'all').default('all'),
  month: Joi.string().pattern(/^\d{4}-\d{2}$/).optional(),
  page: Joi.number().integer().min(1).default(1),
  page_size: Joi.number().integer().min(1).max(200).default(50),
});

const recordPaymentSchema = Joi.object({
  amount: Joi.number().min(0.01).required(),
  method: Joi.string().valid('cash', 'upi', 'bank_transfer', 'card', 'other').default('cash'),
  paid_at: Joi.date().iso().optional(),
  reference: Joi.string().trim().max(120).allow(null, ''),
  notes: Joi.string().trim().max(500).allow(null, ''),
});

// Bulk generate one or many monthly bills for one or many tenants at once.
const bulkGenerateSchema = Joi.object({
  tenant_ids: Joi.array().items(Joi.string().uuid()).optional(),
  generate_for_all_active: Joi.boolean().default(false),
  category: Joi.string().valid(...CATEGORIES).required(),
  billing_month: Joi.string().pattern(/^\d{4}-\d{2}$/).required(),
  due_day: Joi.number().integer().min(1).max(28).default(10),
  amount: Joi.number().min(0).required(),
  description: Joi.string().trim().max(500).allow(null, ''),
  skip_if_exists: Joi.boolean().default(true),
}).or('tenant_ids', 'generate_for_all_active');

module.exports = {
  CATEGORIES,
  createBillSchema,
  updateBillSchema,
  listBillsSchema,
  recordPaymentSchema,
  bulkGenerateSchema,
};
