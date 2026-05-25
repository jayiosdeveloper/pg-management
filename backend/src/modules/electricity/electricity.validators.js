const Joi = require('joi');

const createReadingSchema = Joi.object({
  room_id: Joi.string().uuid().required(),
  billing_month: Joi.string().pattern(/^\d{4}-\d{2}$/).required(),
  start_reading: Joi.number().min(0).required(),
  end_reading: Joi.number().min(0).required(),
  rate_per_unit: Joi.number().min(0).required(),
  notes: Joi.string().trim().max(500).allow(null, ''),
  due_day: Joi.number().integer().min(1).max(28).default(10),
});

const listReadingsSchema = Joi.object({
  billing_month: Joi.string().pattern(/^\d{4}-\d{2}$/).required(),
});

module.exports = { createReadingSchema, listReadingsSchema };
