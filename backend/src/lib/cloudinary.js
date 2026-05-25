const cloudinary = require('cloudinary').v2;
const env = require('../config/env');

let configured = false;

const ensureConfigured = () => {
  if (configured) return true;
  const { cloudName, apiKey, apiSecret } = env.cloudinary;
  if (!cloudName || !apiKey || !apiSecret) return false;
  cloudinary.config({
    cloud_name: cloudName,
    api_key: apiKey,
    api_secret: apiSecret,
    secure: true,
  });
  configured = true;
  return true;
};

/**
 * Uploads a buffer to Cloudinary under the configured folder, returning the
 * secure URL and public_id. Throws if Cloudinary is not configured.
 */
const uploadBuffer = (buffer, { folder = env.cloudinary.folder, publicId, resourceType = 'image' } = {}) => {
  if (!ensureConfigured()) {
    return Promise.reject(new Error('Cloudinary is not configured. Set CLOUDINARY_* env vars.'));
  }
  return new Promise((resolve, reject) => {
    const stream = cloudinary.uploader.upload_stream(
      { folder, public_id: publicId, resource_type: resourceType, overwrite: true },
      (err, result) => (err ? reject(err) : resolve(result)),
    );
    stream.end(buffer);
  });
};

const destroy = async (publicId, resourceType = 'image') => {
  if (!ensureConfigured()) return;
  try {
    await cloudinary.uploader.destroy(publicId, { resource_type: resourceType });
  } catch (_) { /* best-effort */ }
};

/** Pull the public_id back out of a stored Cloudinary URL (best-effort). */
const publicIdFromUrl = (url) => {
  if (!url) return null;
  try {
    const u = new URL(url);
    // .../upload/v1234/<folder>/<file>.<ext>
    const m = u.pathname.match(/upload\/(?:v\d+\/)?(.+)\.[a-z0-9]+$/i);
    return m ? m[1] : null;
  } catch (_) { return null; }
};

module.exports = { uploadBuffer, destroy, publicIdFromUrl, ensureConfigured };
