const router = require('express').Router();
const supabase = require('../../lib/supabase');
const { requireAuth, requireRole } = require('../../middleware/auth');
const asyncHandler = require('../../utils/asyncHandler');
const { ok } = require('../../utils/response');

router.use(requireAuth, requireRole('admin'));

router.get('/overview', asyncHandler(async (req, res) => {
  const [{ count: totalTenants }, { count: activeTenants }, { data: rooms }, { data: bills }, { data: payments }] = await Promise.all([
    supabase.from('tenants').select('id', { count: 'exact', head: true }),
    supabase.from('tenants').select('id', { count: 'exact', head: true }).eq('status', 'active'),
    supabase.from('rooms').select('id, capacity, status'),
    supabase.from('bills').select('amount, amount_paid, status, billing_month'),
    supabase.from('payments').select('amount, paid_at'),
  ]);

  const totalRooms = rooms?.length || 0;
  const occupiedRooms = (rooms || []).filter(r => r.status !== 'vacant').length;
  const totalCapacity = (rooms || []).reduce((a, r) => a + r.capacity, 0);

  const billed = (bills || []).reduce((a, b) => a + Number(b.amount), 0);
  const paid = (bills || []).reduce((a, b) => a + Number(b.amount_paid), 0);
  const pending = Math.max(0, billed - paid);
  const overdue = (bills || []).filter(b => b.status === 'overdue')
    .reduce((a, b) => a + (Number(b.amount) - Number(b.amount_paid)), 0);

  const monthly = {};
  for (const p of payments || []) {
    const m = (p.paid_at || '').slice(0, 7);
    if (!m) continue;
    monthly[m] = (monthly[m] || 0) + Number(p.amount);
  }
  const monthlyIncome = Object.entries(monthly).sort().slice(-6).map(([month, value]) => ({ month, value }));

  return ok(res, {
    counts: { totalTenants, activeTenants, totalRooms, occupiedRooms, totalCapacity },
    billing: { totalBilled: billed, totalPaid: paid, pending, overdue },
    monthlyIncome,
  });
}));

module.exports = router;
