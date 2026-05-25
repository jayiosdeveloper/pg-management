class AppError extends Error {
  constructor(message, statusCode = 500, code = 'INTERNAL_ERROR', details = undefined) {
    super(message);
    this.statusCode = statusCode;
    this.code = code;
    this.details = details;
    this.isOperational = true;
    Error.captureStackTrace?.(this, this.constructor);
  }
}

const BadRequest = (msg, details) => new AppError(msg, 400, 'BAD_REQUEST', details);
const Unauthorized = (msg = 'Unauthorized') => new AppError(msg, 401, 'UNAUTHORIZED');
const Forbidden = (msg = 'Forbidden') => new AppError(msg, 403, 'FORBIDDEN');
const NotFound = (msg = 'Not found') => new AppError(msg, 404, 'NOT_FOUND');
const Conflict = (msg, details) => new AppError(msg, 409, 'CONFLICT', details);
const Internal = (msg = 'Internal server error') => new AppError(msg, 500, 'INTERNAL_ERROR');

module.exports = { AppError, BadRequest, Unauthorized, Forbidden, NotFound, Conflict, Internal };
