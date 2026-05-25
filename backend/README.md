# PG / Hostel Management — Backend

Node.js + Express + Supabase backend for the PG Management Android app.

This is **Phase 1** of a phased build. Auth is fully working here; later phases (Phase 3+) add tenant/room/billing/PDF/notification modules to this same backend.

---

## Stack

- Node.js 18+ / Express 4
- Supabase (PostgreSQL) — database
- Cloudinary — file/image storage (used from Phase 3 onward)
- Firebase Cloud Messaging — push notifications (Phase 8)
- JWT (access + refresh) authentication
- Joi validation, Helmet, CORS, rate limiting

---

## What you need to do (one-time setup)

Follow these steps **in order**. After this, everything else is automated.

### 1. Create a Supabase project (free)

1. Go to https://supabase.com and sign up (free account, GitHub login works).
2. Click **New project**.
3. Pick a project name (e.g. `pg-management`), pick a strong DB password, choose a region close to you.
4. Wait ~2 minutes for provisioning.
5. Once ready, go to **Project Settings → API**. You'll need these three values:
   - **Project URL** (e.g. `https://xyzabc.supabase.co`)
   - **anon public** key
   - **service_role** key (under "Project API keys" — click "Reveal" to see it). **Treat this like a password — never commit it.**

### 2. Run the database migration

1. In the Supabase dashboard, open **SQL Editor → New query**.
2. Open the file `db/migrations/001_initial_schema.sql` from this repo, copy the entire contents.
3. Paste into the Supabase SQL editor and click **Run**.
4. You should see "Success. No rows returned." — all tables are now created.

### 3. Install dependencies and configure

```bash
cd backend
npm install
cp .env.example .env
```

Now open `.env` in any text editor and fill in:

- `SUPABASE_URL` — your Project URL from step 1
- `SUPABASE_ANON_KEY` — anon key from step 1
- `SUPABASE_SERVICE_ROLE_KEY` — service_role key from step 1
- `JWT_ACCESS_SECRET` and `JWT_REFRESH_SECRET` — generate with:
  ```bash
  node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"
  ```
  Run that command twice and paste each output into the two JWT secret fields.
- `SEED_ADMIN_EMAIL` / `SEED_ADMIN_PASSWORD` — pick your admin login. **Change the default password!**

You can leave Cloudinary and Firebase blank for now; we'll fill them in during Phase 3 and Phase 8.

### 4. Create the first admin user

```bash
npm run seed:admin
```

This creates exactly one admin in the `users` table using the credentials from your `.env`. The script prints the login credentials at the end — save them.

### 5. Run the server

```bash
npm run dev
```

You should see:
```
[pg-management] listening on http://localhost:4000/api/v1
```

Open http://localhost:4000/api/v1/health in a browser — should return `{ "success": true, "message": "PG Management API is alive", ... }`.

---

## Test the auth endpoints

Open another terminal and try:

```bash
# Login as admin (use the email + password from your .env)
curl -X POST http://localhost:4000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"admin@pg.local","password":"ChangeMe@123"}'
```

You'll get back `{ "success": true, ..., "data": { "user": {...}, "access_token": "...", "refresh_token": "..." } }`.

Grab the `access_token` and try `/me`:

```bash
curl http://localhost:4000/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

If both work — **Phase 1 is complete**. Move on to Phase 2 (Android setup).

---

## API endpoints (Phase 1)

All routes prefixed with `/api/v1`.

| Method | Path                  | Auth         | Description                                        |
|-------:|-----------------------|--------------|----------------------------------------------------|
|   GET  | /health               | —            | Health check                                       |
|  POST  | /auth/login           | —            | Login with email (admin) or user_code (tenant)     |
|  POST  | /auth/refresh         | —            | Exchange refresh_token for a new access_token      |
|  POST  | /auth/logout          | Bearer       | Revoke refresh tokens                              |
|   GET  | /auth/me              | Bearer       | Current user profile (tenant includes room/bed)    |
|  POST  | /auth/change-password | Bearer       | Self-service password change                       |
|  POST  | /auth/reset-password  | Bearer admin | Admin resets any user's password                   |

Login body:
```json
{
  "identifier": "admin@pg.local",     // email for admin, user_code (e.g. "T-AB12CD") for tenant
  "password": "ChangeMe@123",
  "fcm_token": "optional_fcm_token"
}
```

---

## Project structure

```
backend/
├── db/migrations/        # SQL schema files
├── scripts/              # seed scripts, ops tools
└── src/
    ├── config/           # env loader
    ├── lib/              # supabase, cloudinary, firebase clients
    ├── middleware/       # auth, validation, error, rate limit
    ├── modules/
    │   ├── auth/         # (Phase 1)
    │   ├── tenant/       # (Phase 3)
    │   ├── room/         # (Phase 4)
    │   ├── billing/      # (Phase 5)
    │   ├── payment/      # (Phase 5)
    │   ├── pdf/          # (Phase 6)
    │   ├── notification/ # (Phase 8)
    │   ├── complaint/    # (Phase 9)
    │   ├── visitor/      # (Phase 9)
    │   └── analytics/    # (Phase 9)
    ├── routes/           # central router
    ├── utils/            # errors, jwt, codes, responses
    ├── app.js            # Express app config
    └── server.js         # entry point
```

---

## Hosting (later)

When ready to deploy, we'll use **Render** (free tier, supports Node.js). Phase 1 just runs locally — no hosting needed yet.
