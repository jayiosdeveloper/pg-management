const { customAlphabet } = require('nanoid');

// Avoid lookalikes (0/O, 1/I). 6 chars -> ~2B possibilities; collisions checked at DB layer.
const tenantCodeGen = customAlphabet('ABCDEFGHJKMNPQRSTUVWXYZ23456789', 6);
const tempPasswordGen = customAlphabet('ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$', 12);
const invoiceSeqGen = customAlphabet('0123456789', 6);

const newTenantCode = () => `T-${tenantCodeGen()}`;
const newTempPassword = () => tempPasswordGen();
const newInvoiceNumber = () => {
  const d = new Date();
  const yyyymm = `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}`;
  return `INV-${yyyymm}-${invoiceSeqGen()}`;
};

module.exports = { newTenantCode, newTempPassword, newInvoiceNumber };
