const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const compression = require('compression');
const morgan = require('morgan');
const env = require('./config/env');
const routes = require('./routes');
const { notFoundHandler, errorHandler } = require('./middleware/errorHandler');
const { generalLimiter } = require('./middleware/rateLimit');

const app = express();

app.set('trust proxy', 1);

app.use(helmet());
app.use(compression());
app.use(express.json({ limit: '5mb' }));
app.use(express.urlencoded({ extended: true }));

if (env.cors.allowedOrigins.length === 0) {
  app.use(cors());
} else {
  app.use(cors({
    origin: (origin, cb) => {
      if (!origin) return cb(null, true);
      if (env.cors.allowedOrigins.includes(origin)) return cb(null, true);
      return cb(new Error('CORS not allowed for ' + origin));
    },
    credentials: true,
  }));
}

if (!env.isProd()) app.use(morgan('dev'));

app.use(generalLimiter);

app.use(env.apiPrefix, routes);

app.get('/', (req, res) => {
  res.json({ success: true, name: 'PG Management API', version: '0.1.0', docs: env.apiPrefix + '/health' });
});

app.use(notFoundHandler);
app.use(errorHandler);

module.exports = app;
