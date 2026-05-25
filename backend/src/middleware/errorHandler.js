const env = require('../config/env');
const { AppError } = require('../utils/errors');

// 404 fallthrough
const notFoundHandler = (req, res, next) => {
  res.status(404).json({
    success: false,
    code: 'NOT_FOUND',
    message: `Route ${req.method} ${req.originalUrl} not found`,
  });
};

// Central error handler
// eslint-disable-next-line no-unused-vars
const errorHandler = (err, req, res, next) => {
  const isApp = err instanceof AppError;
  const status = isApp ? err.statusCode : 500;
  const code = isApp ? err.code : 'INTERNAL_ERROR';
  const message = isApp ? err.message : 'Internal server error';

  if (!isApp || status >= 500) {
    // Log unexpected errors
    // eslint-disable-next-line no-console
    console.error('[error]', err);
  }

  const body = { success: false, code, message };
  if (isApp && err.details) body.details = err.details;
  if (!env.isProd() && !isApp) body.stack = err.stack;

  res.status(status).json(body);
};

module.exports = { notFoundHandler, errorHandler };
