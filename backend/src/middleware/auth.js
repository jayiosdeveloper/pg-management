const { verifyAccess } = require('../utils/jwt');
const { Unauthorized, Forbidden } = require('../utils/errors');
const supabase = require('../lib/supabase');

const requireAuth = async (req, res, next) => {
  try {
    const header = req.headers.authorization || '';
    if (!header.startsWith('Bearer ')) throw Unauthorized('Missing Bearer token');
    const token = header.slice(7);

    let decoded;
    try {
      decoded = verifyAccess(token);
    } catch (e) {
      throw Unauthorized('Invalid or expired token');
    }

    const { data: user, error } = await supabase
      .from('users')
      .select('id, role, full_name, email, user_code, is_active')
      .eq('id', decoded.sub)
      .single();

    if (error || !user) throw Unauthorized('User not found');
    if (!user.is_active) throw Forbidden('Account is disabled');

    req.user = user;
    next();
  } catch (e) {
    next(e);
  }
};

const requireRole = (...allowed) => (req, res, next) => {
  if (!req.user) return next(Unauthorized());
  if (!allowed.includes(req.user.role)) return next(Forbidden('Insufficient permissions'));
  next();
};

module.exports = { requireAuth, requireRole };
