const multer = require('multer');
const { BadRequest } = require('../utils/errors');

const MAX_BYTES = 8 * 1024 * 1024; // 8 MB
const ALLOWED_MIME = /^image\/(png|jpeg|jpg|webp|heic|heif)$/i;

const memoryStorage = multer.memoryStorage();

const fileFilter = (req, file, cb) => {
  if (!ALLOWED_MIME.test(file.mimetype)) {
    return cb(new BadRequest(`Unsupported file type: ${file.mimetype}`));
  }
  cb(null, true);
};

const imageUpload = multer({
  storage: memoryStorage,
  limits: { fileSize: MAX_BYTES, files: 5 },
  fileFilter,
});

module.exports = { imageUpload, MAX_BYTES };
