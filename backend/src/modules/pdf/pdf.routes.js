const router = require('express').Router();
const Joi = require('joi');
const validate = require('../../middleware/validate');
const { requireAuth, requireRole } = require('../../middleware/auth');
const asyncHandler = require('../../utils/asyncHandler');
const { ok, created } = require('../../utils/response');
const service = require('./pdf.service');

const generateSchema = Joi.object({
  tenant_id: Joi.string().uuid().required(),
  billing_month: Joi.string().pattern(/^\d{4}-\d{2}$/).required(),
});

router.use(requireAuth);

router.get('/', asyncHandler(async (req, res) => {
  const data = await service.list(req.query, req.user);
  return ok(res, data);
}));

router.post('/generate', requireRole('admin'), validate(generateSchema), asyncHandler(async (req, res) => {
  const result = await service.generateForTenantMonth(req.body.tenant_id, req.body.billing_month, req.user);
  return created(res, {
    invoice_number: result.invoiceNumber,
    pdf_url: result.pdfUrl,
    totals: result.totals,
  }, 'Invoice generated');
}));

// Stream the PDF directly (used by Android download flow)
router.get('/:tenantId/:billingMonth.pdf', asyncHandler(async (req, res) => {
  const result = await service.generateForTenantMonth(req.params.tenantId, req.params.billingMonth, req.user);
  res.setHeader('Content-Type', 'application/pdf');
  res.setHeader('Content-Disposition', `inline; filename="${result.invoiceNumber}.pdf"`);
  res.send(result.pdfBuffer);
}));

module.exports = router;
