const { customAlphabet } = require('nanoid');

// Avoid lookalikes (0/O, 1/I). 6 chars -> ~2B possibilities; collisions checked at DB layer.
const tenantCodeGen = customAlphabet('ABCDEFGHJKMNPQRSTUVWXYZ23456789', 6);
const invoiceSeqGen = customAlphabet('0123456789', 6);
const digitGen = customAlphabet('23456789', 4);  // skip 0/1 for clarity

// Pool of short, easy-to-remember words. Combined with 4 digits these still
// give roughly 5 million possibilities, which is plenty for a PG admin tool.
const PASSWORD_WORDS = [
  'Sun', 'Sky', 'Tree', 'Star', 'Moon', 'Wave', 'Wind', 'Rain', 'Lake', 'River',
  'Hill', 'Rose', 'Lion', 'Tiger', 'Eagle', 'Falcon', 'Otter', 'Robin', 'Whale',
  'Coral', 'Pearl', 'Jade', 'Ruby', 'Amber', 'Silver', 'Golden', 'Maple', 'Pine',
  'Cedar', 'Quartz', 'Onyx', 'Ivory', 'Crimson', 'Indigo', 'Violet', 'Saffron',
  'Mango', 'Lotus', 'Orchid', 'Jasmine', 'Lily', 'Daisy', 'Tulip', 'Poppy',
];
const pickWord = () => PASSWORD_WORDS[Math.floor(Math.random() * PASSWORD_WORDS.length)];

const newTenantCode = () => `T-${tenantCodeGen()}`;
const newWorkerCode = () => `W-${tenantCodeGen()}`;

/**
 * Friendly password: Word + 4 digits, e.g. "Sun4823" or "Maple9276".
 * Easy to read out over the phone and tap on a keyboard.
 */
const newTempPassword = () => `${pickWord()}${digitGen()}`;

const newInvoiceNumber = () => {
  const d = new Date();
  const yyyymm = `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}`;
  return `INV-${yyyymm}-${invoiceSeqGen()}`;
};

module.exports = { newTenantCode, newWorkerCode, newTempPassword, newInvoiceNumber };
