// Runs the initial SQL migration directly against the Supabase Postgres database.
// Requires DATABASE_URL in .env (Supabase Settings -> Database -> Connection string -> URI).

require('dotenv').config();
const fs = require('fs');
const path = require('path');
const { Client } = require('pg');

const DATABASE_URL = process.env.DATABASE_URL;
if (!DATABASE_URL || DATABASE_URL.startsWith('PASTE_')) {
  console.error('[migrate] DATABASE_URL missing or placeholder in .env. Fix it and re-run.');
  process.exit(1);
}

const MIGRATION = path.join(__dirname, '..', 'db', 'migrations', '001_initial_schema.sql');
const sql = fs.readFileSync(MIGRATION, 'utf-8');

(async () => {
  const client = new Client({
    connectionString: DATABASE_URL,
    ssl: { rejectUnauthorized: false },
  });
  try {
    console.log('[migrate] Connecting...');
    await client.connect();
    console.log('[migrate] Running 001_initial_schema.sql ...');
    await client.query(sql);
    console.log('[migrate] ✓ Migration complete');

    // Force PostgREST schema cache reload so supabase-js sees the new tables immediately.
    await client.query("NOTIFY pgrst, 'reload schema';");
    console.log('[migrate] ✓ PostgREST schema cache reloaded');

    const { rows } = await client.query(
      "select tablename from pg_tables where schemaname = 'public' order by tablename;"
    );
    console.log('\n[migrate] Tables now in public schema:');
    rows.forEach((r) => console.log('  -', r.tablename));
  } catch (e) {
    console.error('[migrate] ✘ Failed:', e.message);
    process.exit(1);
  } finally {
    await client.end();
  }
})();
