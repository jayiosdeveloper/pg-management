const supabase = require('../../lib/supabase');
const cloudinary = require('../../lib/cloudinary');
const { newInvoiceNumber } = require('../../utils/codes');
const { NotFound } = require('../../utils/errors');
const { generate } = require('./invoice.generator');

const TENANT_FOR_PDF = `
  id, joining_date, bed:beds(id, bed_label),
  user:users!tenants_user_id_fkey ( id, full_name, user_code, email, phone ),
  room:rooms ( id, room_number )
`;

const generateForTenantMonth = async (tenantId, billingMonth, generatedBy) => {
  const { data: tenant } = await supabase.from('tenants').select(TENANT_FOR_PDF).eq('id', tenantId).maybeSingle();
  if (!tenant) throw NotFound('Tenant not found');

  const monthDate = `${billingMonth}-01`;
  const { data: bills } = await supabase
    .from('bills')
    .select('id, category, amount, amount_paid, billing_month, due_date, description, status')
    .eq('tenant_id', tenantId).eq('billing_month', monthDate);

  const { data: payments } = await supabase
    .from('payments')
    .select('amount, method, paid_at, reference')
    .eq('tenant_id', tenantId)
    .gte('paid_at', `${billingMonth}-01`)
    .lte('paid_at', `${billingMonth}-31`)
    .order('paid_at', { ascending: true });

  const totalBilled = (bills || []).reduce((acc, b) => acc + Number(b.amount), 0);
  const totalPaid = (bills || []).reduce((acc, b) => acc + Number(b.amount_paid), 0);
  const totals = { totalBilled, totalPaid, pending: Math.max(0, totalBilled - totalPaid) };
  const invoiceNumber = newInvoiceNumber();

  const pdfBuffer = await generate({
    tenant, bills: bills || [], payments: payments || [], invoiceNumber, billingMonth, totals,
  });

  // Upload to Cloudinary if configured, else fall back to a data URL via base64.
  let pdfUrl = null;
  let publicId = null;
  if (cloudinary.ensureConfigured()) {
    const uploaded = await cloudinary.uploadBuffer(pdfBuffer, {
      folder: `pg-management/invoices`,
      publicId: `${tenantId}_${billingMonth}_${invoiceNumber}`,
      resourceType: 'raw',
    });
    pdfUrl = uploaded.secure_url;
    publicId = uploaded.public_id;
  }

  await supabase.from('invoices').insert({
    tenant_id: tenantId,
    invoice_number: invoiceNumber,
    billing_month: monthDate,
    total_amount: totalBilled,
    paid_amount: totalPaid,
    pending_amount: totals.pending,
    pdf_url: pdfUrl,
    generated_by: generatedBy?.id || null,
  });

  return { invoiceNumber, pdfUrl, publicId, pdfBuffer, totals };
};

const list = async ({ tenant_id }, currentUser) => {
  let q = supabase.from('invoices').select(`
    id, tenant_id, invoice_number, billing_month, total_amount, paid_amount, pending_amount, pdf_url, created_at,
    tenant:tenants ( id, user:users!tenants_user_id_fkey ( full_name, user_code ) )
  `).order('created_at', { ascending: false });

  if (currentUser?.role === 'tenant') {
    const { data: t } = await supabase.from('tenants').select('id').eq('user_id', currentUser.id).maybeSingle();
    if (!t) return [];
    q = q.eq('tenant_id', t.id);
  } else if (tenant_id) {
    q = q.eq('tenant_id', tenant_id);
  }

  const { data, error } = await q;
  if (error) throw error;
  return data || [];
};

module.exports = { generateForTenantMonth, list };
