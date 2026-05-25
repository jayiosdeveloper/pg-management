-- ============================================================================
-- PG / Hostel Management ERP - Initial Schema
-- Database: Supabase (PostgreSQL 15+)
-- Run this in the Supabase SQL Editor (Dashboard -> SQL Editor -> New query)
-- ============================================================================

-- -- Extensions -----------------------------------------------------------
create extension if not exists "pgcrypto";   -- gen_random_uuid()
create extension if not exists "citext";     -- case-insensitive text (emails)

-- -- Enums ----------------------------------------------------------------
do $$ begin
  create type user_role as enum ('admin', 'tenant');
exception when duplicate_object then null; end $$;

do $$ begin
  create type tenant_status as enum ('active', 'inactive', 'left');
exception when duplicate_object then null; end $$;

do $$ begin
  create type room_status as enum ('vacant', 'partial', 'occupied');
exception when duplicate_object then null; end $$;

do $$ begin
  create type bed_status as enum ('vacant', 'occupied');
exception when duplicate_object then null; end $$;

do $$ begin
  create type bill_category as enum ('rent', 'food', 'cleaning', 'repair', 'electricity', 'water', 'other');
exception when duplicate_object then null; end $$;

do $$ begin
  create type bill_status as enum ('unpaid', 'partial', 'paid', 'overdue');
exception when duplicate_object then null; end $$;

do $$ begin
  create type payment_method as enum ('cash', 'upi', 'bank_transfer', 'card', 'other');
exception when duplicate_object then null; end $$;

do $$ begin
  create type notif_type as enum ('payment_reminder', 'overdue', 'payment_confirmed', 'announcement', 'maintenance', 'other');
exception when duplicate_object then null; end $$;

do $$ begin
  create type complaint_status as enum ('open', 'in_progress', 'resolved', 'closed');
exception when duplicate_object then null; end $$;

do $$ begin
  create type complaint_priority as enum ('low', 'medium', 'high', 'urgent');
exception when duplicate_object then null; end $$;

-- -- Users (Admin + Tenants share this) ----------------------------------
create table if not exists users (
  id              uuid primary key default gen_random_uuid(),
  user_code       text unique not null,                    -- short login code generated for tenants (e.g. T-AB12CD)
  email           citext unique,
  phone           text,
  password_hash   text not null,
  role            user_role not null default 'tenant',
  full_name       text not null,
  is_active       boolean not null default true,
  fcm_token       text,
  last_login_at   timestamptz,
  created_at      timestamptz not null default now(),
  updated_at      timestamptz not null default now()
);
create index if not exists idx_users_role on users(role);
create index if not exists idx_users_active on users(is_active);

-- -- Rooms ----------------------------------------------------------------
create table if not exists rooms (
  id              uuid primary key default gen_random_uuid(),
  room_number     text unique not null,
  floor           int,
  capacity        int not null default 1 check (capacity > 0),
  monthly_rent    numeric(10,2) not null default 0,
  description     text,
  status          room_status not null default 'vacant',
  created_at      timestamptz not null default now(),
  updated_at      timestamptz not null default now()
);

create table if not exists beds (
  id              uuid primary key default gen_random_uuid(),
  room_id         uuid not null references rooms(id) on delete cascade,
  bed_label       text not null,                          -- "A", "B", "1", "2", etc.
  status          bed_status not null default 'vacant',
  created_at      timestamptz not null default now(),
  unique(room_id, bed_label)
);
create index if not exists idx_beds_room on beds(room_id);
create index if not exists idx_beds_status on beds(status);

-- -- Tenants (extends users) ---------------------------------------------
create table if not exists tenants (
  id                    uuid primary key default gen_random_uuid(),
  user_id               uuid unique not null references users(id) on delete cascade,
  room_id               uuid references rooms(id) on delete set null,
  bed_id                uuid unique references beds(id) on delete set null,
  date_of_birth         date,
  gender                text,
  address               text,
  city                  text,
  state                 text,
  emergency_contact_name  text,
  emergency_contact_phone text,
  occupation            text,
  id_proof_type         text,                              -- "aadhaar", "pan", etc.
  id_proof_number       text,
  photo_url             text,                              -- Cloudinary
  aadhaar_front_url     text,
  aadhaar_back_url      text,
  joining_date          date not null default current_date,
  leaving_date          date,
  monthly_rent          numeric(10,2),                     -- override per tenant; if null use room's rent
  security_deposit      numeric(10,2) default 0,
  status                tenant_status not null default 'active',
  notes                 text,
  created_at            timestamptz not null default now(),
  updated_at            timestamptz not null default now()
);
create index if not exists idx_tenants_status on tenants(status);
create index if not exists idx_tenants_room on tenants(room_id);

-- -- Bills ---------------------------------------------------------------
create table if not exists bills (
  id              uuid primary key default gen_random_uuid(),
  tenant_id       uuid not null references tenants(id) on delete cascade,
  category        bill_category not null,
  amount          numeric(10,2) not null check (amount >= 0),
  amount_paid     numeric(10,2) not null default 0 check (amount_paid >= 0),
  billing_month   date not null,                           -- first-of-month
  due_date        date not null,
  description     text,
  status          bill_status not null default 'unpaid',
  created_at      timestamptz not null default now(),
  updated_at      timestamptz not null default now()
);
create index if not exists idx_bills_tenant on bills(tenant_id);
create index if not exists idx_bills_status on bills(status);
create index if not exists idx_bills_month on bills(billing_month);
create index if not exists idx_bills_due on bills(due_date);

-- -- Payments ------------------------------------------------------------
create table if not exists payments (
  id              uuid primary key default gen_random_uuid(),
  bill_id         uuid not null references bills(id) on delete cascade,
  tenant_id       uuid not null references tenants(id) on delete cascade,
  amount          numeric(10,2) not null check (amount > 0),
  method          payment_method not null default 'cash',
  paid_at         timestamptz not null default now(),
  reference       text,                                    -- UPI ref, txn id, etc.
  notes           text,
  recorded_by     uuid references users(id),               -- admin who recorded
  created_at      timestamptz not null default now()
);
create index if not exists idx_payments_bill on payments(bill_id);
create index if not exists idx_payments_tenant on payments(tenant_id);
create index if not exists idx_payments_date on payments(paid_at);

-- -- Notifications -------------------------------------------------------
create table if not exists notifications (
  id              uuid primary key default gen_random_uuid(),
  user_id         uuid references users(id) on delete cascade,  -- null = broadcast to all tenants
  title           text not null,
  body            text not null,
  type            notif_type not null default 'announcement',
  data            jsonb default '{}'::jsonb,
  is_read         boolean not null default false,
  sent_at         timestamptz not null default now(),
  read_at         timestamptz
);
create index if not exists idx_notifications_user on notifications(user_id);
create index if not exists idx_notifications_unread on notifications(user_id, is_read);

-- -- Complaints ----------------------------------------------------------
create table if not exists complaints (
  id              uuid primary key default gen_random_uuid(),
  tenant_id       uuid not null references tenants(id) on delete cascade,
  title           text not null,
  description     text not null,
  category        text,                                    -- "maintenance", "cleanliness", "noise", etc.
  priority        complaint_priority not null default 'medium',
  status          complaint_status not null default 'open',
  admin_response  text,
  resolved_at     timestamptz,
  created_at      timestamptz not null default now(),
  updated_at      timestamptz not null default now()
);
create index if not exists idx_complaints_tenant on complaints(tenant_id);
create index if not exists idx_complaints_status on complaints(status);

-- -- Visitor logs --------------------------------------------------------
create table if not exists visitor_logs (
  id              uuid primary key default gen_random_uuid(),
  tenant_id       uuid not null references tenants(id) on delete cascade,
  visitor_name    text not null,
  visitor_phone   text,
  purpose         text,
  entry_time      timestamptz not null default now(),
  exit_time       timestamptz,
  recorded_by     uuid references users(id),
  notes           text
);
create index if not exists idx_visitor_logs_tenant on visitor_logs(tenant_id);
create index if not exists idx_visitor_logs_entry on visitor_logs(entry_time);

-- -- Entry/exit logs (security) -----------------------------------------
create table if not exists entry_logs (
  id              uuid primary key default gen_random_uuid(),
  tenant_id       uuid not null references tenants(id) on delete cascade,
  event_type      text not null check (event_type in ('entry','exit')),
  event_time      timestamptz not null default now(),
  notes           text
);
create index if not exists idx_entry_logs_tenant on entry_logs(tenant_id);
create index if not exists idx_entry_logs_time on entry_logs(event_time);

-- -- Invoices (generated PDFs) ------------------------------------------
create table if not exists invoices (
  id              uuid primary key default gen_random_uuid(),
  tenant_id       uuid not null references tenants(id) on delete cascade,
  invoice_number  text unique not null,
  billing_month   date not null,
  total_amount    numeric(10,2) not null,
  paid_amount     numeric(10,2) not null default 0,
  pending_amount  numeric(10,2) not null default 0,
  pdf_url         text,                                    -- Cloudinary URL of generated PDF
  generated_by    uuid references users(id),
  created_at      timestamptz not null default now()
);
create index if not exists idx_invoices_tenant on invoices(tenant_id);
create index if not exists idx_invoices_month on invoices(billing_month);

-- -- Refresh tokens (for JWT rotation) ----------------------------------
create table if not exists refresh_tokens (
  id              uuid primary key default gen_random_uuid(),
  user_id         uuid not null references users(id) on delete cascade,
  token_hash      text not null,
  expires_at      timestamptz not null,
  revoked_at      timestamptz,
  created_at      timestamptz not null default now()
);
create index if not exists idx_refresh_tokens_user on refresh_tokens(user_id);
create index if not exists idx_refresh_tokens_hash on refresh_tokens(token_hash);

-- ============================================================================
-- Triggers: auto-update updated_at columns
-- ============================================================================
create or replace function set_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at := now();
  return new;
end $$;

do $$ declare t text; begin
  for t in select unnest(array['users','rooms','tenants','bills','complaints']) loop
    execute format('drop trigger if exists trg_%I_updated on %I', t, t);
    execute format('create trigger trg_%I_updated before update on %I for each row execute function set_updated_at()', t, t);
  end loop;
end $$;

-- ============================================================================
-- Helper: keep bills.status in sync with amount_paid
-- ============================================================================
create or replace function refresh_bill_status()
returns trigger language plpgsql as $$
begin
  if new.amount_paid >= new.amount then
    new.status := 'paid';
  elsif new.amount_paid > 0 then
    new.status := 'partial';
  elsif new.due_date < current_date then
    new.status := 'overdue';
  else
    new.status := 'unpaid';
  end if;
  return new;
end $$;

drop trigger if exists trg_bill_status on bills;
create trigger trg_bill_status before insert or update of amount_paid, due_date on bills
for each row execute function refresh_bill_status();

-- ============================================================================
-- Note on RLS:
-- We use the service_role key on the backend (full access). All authorization
-- happens at the Express middleware layer. So we do NOT enable RLS here; that
-- keeps queries simple and the backend stays the single security boundary.
-- ============================================================================
