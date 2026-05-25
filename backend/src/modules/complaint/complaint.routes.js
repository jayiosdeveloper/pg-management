const router = require('express').Router();
const Joi = require('joi');
const supabase = require('../../lib/supabase');
const { requireAuth, requireRole } = require('../../middleware/auth');
const validate = require('../../middleware/validate');
const asyncHandler = require('../../utils/asyncHandler');
const { ok, created } = require('../../utils/response');
const { NotFound, Forbidden } = require('../../utils/errors');

const createSchema = Joi.object({
  title: Joi.string().trim().min(2).max(150).required(),
  description: Joi.string().trim().min(2).max(2000).required(),
  category: Joi.string().trim().max(60).optional(),
  priority: Joi.string().valid('low', 'medium', 'high', 'urgent').default('medium'),
});

const updateSchema = Joi.object({
  status: Joi.string().valid('open', 'in_progress', 'resolved', 'closed').optional(),
  admin_response: Joi.string().trim().max(2000).allow(null, ''),
  priority: Joi.string().valid('low', 'medium', 'high', 'urgent').optional(),
});

const CSEL = `
  id, tenant_id, title, description, category, priority, status, admin_response, resolved_at, created_at, updated_at,
  tenant:tenants ( id, user:users!tenants_user_id_fkey ( id, full_name, user_code ), room:rooms ( id, room_number ) )
`;

const tenantOf = async (userId) => {
  const { data } = await supabase.from('tenants').select('id').eq('user_id', userId).maybeSingle();
  return data?.id || null;
};

router.use(requireAuth);

router.get('/', asyncHandler(async (req, res) => {
  let q = supabase.from('complaints').select(CSEL).order('created_at', { ascending: false });
  if (req.user.role === 'tenant') {
    const tid = await tenantOf(req.user.id);
    if (!tid) return ok(res, []);
    q = q.eq('tenant_id', tid);
  } else if (req.query.tenant_id) {
    q = q.eq('tenant_id', req.query.tenant_id);
  }
  if (req.query.status && req.query.status !== 'all') q = q.eq('status', req.query.status);
  const { data, error } = await q;
  if (error) throw error;
  return ok(res, data || []);
}));

router.post('/', validate(createSchema), asyncHandler(async (req, res) => {
  if (req.user.role !== 'tenant') throw Forbidden('Only tenants can submit complaints');
  const tid = await tenantOf(req.user.id);
  if (!tid) throw NotFound('Tenant profile not found');
  const { data, error } = await supabase.from('complaints').insert({
    tenant_id: tid,
    title: req.body.title,
    description: req.body.description,
    category: req.body.category || null,
    priority: req.body.priority,
  }).select(CSEL).single();
  if (error) throw error;
  return created(res, data, 'Complaint submitted');
}));

router.patch('/:id', requireRole('admin'), validate(updateSchema), asyncHandler(async (req, res) => {
  const patch = { ...req.body };
  if (patch.status === 'resolved' || patch.status === 'closed') patch.resolved_at = new Date().toISOString();
  const { data, error } = await supabase.from('complaints').update(patch).eq('id', req.params.id).select(CSEL).single();
  if (error || !data) throw NotFound('Complaint not found');
  return ok(res, data, 'Complaint updated');
}));

router.delete('/:id', requireRole('admin'), asyncHandler(async (req, res) => {
  const { error } = await supabase.from('complaints').delete().eq('id', req.params.id);
  if (error) throw error;
  return ok(res, null, 'Complaint deleted');
}));

module.exports = router;
