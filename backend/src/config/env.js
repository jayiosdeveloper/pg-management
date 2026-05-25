require('dotenv').config();

const required = (key) => {
  const v = process.env[key];
  if (!v || v.startsWith('replace_') || v.startsWith('your_')) {
    throw new Error(`Missing or placeholder env var: ${key}. Edit your .env file.`);
  }
  return v;
};

const optional = (key, fallback) => process.env[key] || fallback;

const env = {
  nodeEnv: optional('NODE_ENV', 'development'),
  port: parseInt(optional('PORT', '4000'), 10),
  apiPrefix: optional('API_PREFIX', '/api/v1'),

  jwt: {
    accessSecret: required('JWT_ACCESS_SECRET'),
    refreshSecret: required('JWT_REFRESH_SECRET'),
    accessExpires: optional('JWT_ACCESS_EXPIRES', '1d'),
    refreshExpires: optional('JWT_REFRESH_EXPIRES', '30d'),
  },

  supabase: {
    url: required('SUPABASE_URL'),
    anonKey: required('SUPABASE_ANON_KEY'),
    serviceRoleKey: required('SUPABASE_SERVICE_ROLE_KEY'),
  },

  cloudinary: {
    cloudName: optional('CLOUDINARY_CLOUD_NAME'),
    apiKey: optional('CLOUDINARY_API_KEY'),
    apiSecret: optional('CLOUDINARY_API_SECRET'),
    folder: optional('CLOUDINARY_FOLDER', 'pg-management'),
  },

  firebase: {
    serviceAccountPath: optional('FIREBASE_SERVICE_ACCOUNT_PATH'),
  },

  seed: {
    adminEmail: optional('SEED_ADMIN_EMAIL', 'admin@pg.local'),
    adminPassword: optional('SEED_ADMIN_PASSWORD', 'ChangeMe@123'),
    adminName: optional('SEED_ADMIN_NAME', 'Super Admin'),
  },

  cors: {
    allowedOrigins: optional('ALLOWED_ORIGINS', '')
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean),
  },

  isProd() {
    return this.nodeEnv === 'production';
  },
};

module.exports = env;
