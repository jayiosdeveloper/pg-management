// One-time bootstrap: creates the initial admin from .env (SEED_ADMIN_*).
// Safe to re-run: it upserts on email.

const env = require('../src/config/env');
const supabase = require('../src/lib/supabase');
const { hashPassword } = require('../src/modules/auth/auth.service');

const ADMIN_USER_CODE = 'ADMIN-001';

(async () => {
  try {
    const password_hash = await hashPassword(env.seed.adminPassword);

    const { data: existing } = await supabase
      .from('users')
      .select('id, email')
      .eq('email', env.seed.adminEmail)
      .maybeSingle();

    if (existing) {
      await supabase.from('users').update({
        password_hash,
        full_name: env.seed.adminName,
        role: 'admin',
        is_active: true,
      }).eq('id', existing.id);
      // eslint-disable-next-line no-console
      console.log(`[seed] Updated existing admin: ${env.seed.adminEmail}`);
    } else {
      const { error } = await supabase.from('users').insert({
        user_code: ADMIN_USER_CODE,
        email: env.seed.adminEmail,
        password_hash,
        role: 'admin',
        full_name: env.seed.adminName,
        is_active: true,
      });
      if (error) throw error;
      // eslint-disable-next-line no-console
      console.log(`[seed] Created admin: ${env.seed.adminEmail}`);
    }

    // eslint-disable-next-line no-console
    console.log('\n=== Login credentials ===');
    // eslint-disable-next-line no-console
    console.log(`identifier: ${env.seed.adminEmail}`);
    // eslint-disable-next-line no-console
    console.log(`password  : ${env.seed.adminPassword}`);
    // eslint-disable-next-line no-console
    console.log('=========================\n');
    process.exit(0);
  } catch (e) {
    // eslint-disable-next-line no-console
    console.error('[seed] Failed:', e.message);
    process.exit(1);
  }
})();
