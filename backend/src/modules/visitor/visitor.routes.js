const router = require('express').Router();
const Joi = require('joi');
const supabase = require('../../lib/supabase');
const { requireAuth, requireRole } = require('../../middleware/auth');
const validate = require('../../middleware/validate');
const asyncHandler = require('../../utils/asyncHandler');
const { ok, created } = require('../../utils/response');
const { NotFound } = require('../../utils/errors');

const createSchema = Joi.object({
  tenant_id: Joi.string().uuid().required(),
  visitor_name: Joi.string().trim().min(2).max(120).required(),
  visitor_phone: Joi.string().trim().max(20).allow(null, ''),
  purpose: Joi.string().trim().max(200).allow(null, ''),
  entry_time: Joi.date().iso().optional(),
  notes: Joi.string().trim().max(500).allow(null, ''),
});

const exitSchema = Joi.object({
  exit_time: Joi.date().iso().optional(),
});

const SEL = `
  id, tenant_id, visitor_name, visitor_phone, purpose, entry_time, exit_time, notes, recorded_by,
  tenant:tenants ( id, user:users!tenants_user_id_fkey ( id, full_name, user_code ), room:rooms ( id, room_number ) )
`;

router.use(requireAuth, requireRole('admin'));

router.get('/', asyncHandler(async (req, res) => {
  let q = supabase.from('visitor_logs').select(SEL).order('entry_time', { ascending: false });
  if (req.query.tenant_id) q = q.eq('tenant_id', req.query.tenant_id);
  if (req.query.active === 'true') q = q.is('exit_time', null);
  const { data, error } = await q;
  if (error) throw error;
  return ok(res, data || []);
}));

router.post('/', validate(createSchema), asyncHandler(async (req, res) => {
  const insert = {
    tenant_id: req.body.tenant_id,
    visitor_name: req.body.visitor_name,
    visitor_phone: req.body.visitor_phone || null,
    purpose: req.body.purpose || null,
    entry_time: req.body.entry_time || new Date().toISOString(),
    notes: req.body.notes || null,
    recorded_by: req.user.id,
  };
  const { data, error } = await supabase.from('visitor_logs').insert(insert).select(SEL).single();
  if (error) throw error;
  return created(res, data, 'Visitor logged');
}));

router.post('/:id/exit', validate(exitSchema), asyncHandler(async (req, res) => {
  const exitAt = req.body.exit_time || new Date().toISOString();
  const { data, error } = await supabase.from('visitor_logs').update({ exit_time: exitAt }).eq('id', req.params.id).select(SEL).single();
  if (error || !data) throw NotFound('Visitor log not found');
  return ok(res, data, 'Exit recorded');
}));

router.delete('/:id', asyncHandler(async (req, res) => {
  const { error } = await supabase.from('visitor_logs').delete().eq('id', req.params.id);
  if (error) throw error;
  return ok(res, null, 'Removed');
}));

module.exports = router;
