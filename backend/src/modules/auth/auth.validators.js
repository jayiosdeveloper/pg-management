const Joi = require('joi');

// Login accepts either email (admin) or user_code (tenant) in the "identifier" field.
const loginSchema = Joi.object({
  identifier: Joi.string().trim().min(3).max(120).required(),
  password: Joi.string().min(6).max(128).required(),
  fcm_token: Joi.string().allow('', null),
});

const refreshSchema = Joi.object({
  refresh_token: Joi.string().required(),
});

const forgotPasswordSchema = Joi.object({
  identifier: Joi.string().trim().required(),
});

const changePasswordSchema = Joi.object({
  current_password: Joi.string().required(),
  new_password: Joi.string().min(8).max(128).required(),
});

const resetPasswordSchema = Joi.object({
  user_id: Joi.string().uuid().required(),
  new_password: Joi.string().min(8).max(128).required(),
});

module.exports = {
  loginSchema,
  refreshSchema,
  forgotPasswordSchema,
  changePasswordSchema,
  resetPasswordSchema,
};
