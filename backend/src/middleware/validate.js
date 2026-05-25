const { BadRequest } = require('../utils/errors');

// Usage: validate(schema, 'body' | 'query' | 'params')
module.exports = (schema, source = 'body') => (req, res, next) => {
  const { value, error } = schema.validate(req[source], { abortEarly: false, stripUnknown: true });
  if (error) {
    return next(
      BadRequest(
        'Validation failed',
        error.details.map((d) => ({ path: d.path.join('.'), message: d.message })),
      ),
    );
  }
  req[source] = value;
  next();
};
