-- ============================================================================
-- 002: Workers + salary payments
-- ============================================================================

-- Add 'worker' to the user role enum
do $$ begin
  if not exists (
    select 1 from pg_enum
    where enumtypid = 'user_role'::regtype and enumlabel = 'worker'
  ) then
    alter type user_role add value 'worker';
  end if;
end $$;

-- Workers ----------------------------------------------------------------
create table if not exists workers (
  id                      uuid primary key default gen_random_uuid(),
  user_id                 uuid unique not null references users(id) on delete cascade,
  role_title              text,                                 -- "cook", "cleaner", "security", "manager", ...
  monthly_salary          numeric(10,2) not null default 0,
  joining_date            date not null default current_date,
  leaving_date            date,
  date_of_birth           date,
  gender                  text,
  address                 text,
  city                    text,
  state                   text,
  emergency_contact_name  text,
  emergency_contact_phone text,
  id_proof_type           text,
  id_proof_number         text,
  photo_url               text,
  aadhaar_front_url       text,
  aadhaar_back_url        text,
  status                  text not null default 'active' check (status in ('active','inactive','left')),
  notes                   text,
  created_at              timestamptz not null default now(),
  updated_at              timestamptz not null default now()
);
create index if not exists idx_workers_status on workers(status);

drop trigger if exists trg_workers_updated on workers;
create trigger trg_workers_updated before update on workers
for each row execute function set_updated_at();

-- Salary payments --------------------------------------------------------
create table if not exists worker_salary_payments (
  id            uuid primary key default gen_random_uuid(),
  worker_id     uuid not null references workers(id) on delete cascade,
  amount        numeric(10,2) not null check (amount > 0),
  pay_for_month date not null,                              -- first-of-month
  method        payment_method not null default 'cash',
  paid_at       timestamptz not null default now(),
  reference     text,
  notes         text,
  recorded_by   uuid references users(id),
  created_at    timestamptz not null default now()
);
create index if not exists idx_wsp_worker on worker_salary_payments(worker_id);
create index if not exists idx_wsp_month on worker_salary_payments(pay_for_month);

-- Refresh PostgREST schema cache so supabase-js sees the new tables
notify pgrst, 'reload schema';
