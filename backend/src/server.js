const app = require('./app');
const env = require('./config/env');

const server = app.listen(env.port, () => {
  // eslint-disable-next-line no-console
  console.log(`[pg-management] listening on http://localhost:${env.port}${env.apiPrefix}`);
});

const shutdown = (signal) => {
  // eslint-disable-next-line no-console
  console.log(`\n[pg-management] received ${signal}, shutting down...`);
  server.close(() => process.exit(0));
  setTimeout(() => process.exit(1), 10_000).unref();
};

['SIGINT', 'SIGTERM'].forEach((sig) => process.on(sig, () => shutdown(sig)));

process.on('unhandledRejection', (err) => {
  // eslint-disable-next-line no-console
  console.error('[unhandledRejection]', err);
});
