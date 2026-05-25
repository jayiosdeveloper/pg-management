// Runs every SQL file in db/migrations/ in lexical order against the
// Supabase Postgres instance. The scripts are written with IF NOT EXISTS
// / idempotent guards so re-running is safe.

require('dotenv').config();
const fs = require('fs');
const path = require('path');
const { Client } = require('pg');

const DATABASE_URL = process.env.DATABASE_URL;
if (!DATABASE_URL || DATABASE_URL.startsWith('PASTE_')) {
  console.error('[migrate] DATABASE_URL missing or placeholder in .env. Fix it and re-run.');
  process.exit(1);
}

const MIGRATIONS_DIR = path.join(__dirname, '..', 'db', 'migrations');
const files = fs.readdirSync(MIGRATIONS_DIR)
  .filter((f) => f.endsWith('.sql'))
  .sort();

(async () => {
  const client = new Client({
    connectionString: DATABASE_URL,
    ssl: { rejectUnauthorized: false },
  });
  try {
    console.log('[migrate] Connecting...');
    await client.connect();
    for (const f of files) {
      console.log(`[migrate] Running ${f} ...`);
      const sql = fs.readFileSync(path.join(MIGRATIONS_DIR, f), 'utf-8');
      await client.query(sql);
      console.log(`[migrate]   ✓ ${f}`);
    }
    await client.query("NOTIFY pgrst, 'reload schema';");
    console.log('[migrate] ✓ PostgREST schema cache reloaded');

    const { rows } = await client.query(
      "select tablename from pg_tables where schemaname = 'public' order by tablename;"
    );
    console.log('\n[migrate] Tables in public schema:');
    rows.forEach((r) => console.log('  -', r.tablename));
  } catch (e) {
    console.error('[migrate] ✘ Failed:', e.message);
    process.exit(1);
  } finally {
    await client.end();
  }
})();
