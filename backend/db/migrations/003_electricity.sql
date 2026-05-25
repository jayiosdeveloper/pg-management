-- ============================================================================
-- 003: Electricity readings per room per month
-- ============================================================================

create table if not exists electricity_readings (
  id                          uuid primary key default gen_random_uuid(),
  room_id                     uuid not null references rooms(id) on delete cascade,
  billing_month               date not null,                       -- first-of-month
  start_reading               numeric(12,2) not null,
  end_reading                 numeric(12,2) not null check (end_reading >= start_reading),
  rate_per_unit               numeric(10,2) not null check (rate_per_unit >= 0),
  units_used                  numeric(12,2) generated always as (end_reading - start_reading) stored,
  total_amount                numeric(12,2) not null,
  per_member_amount           numeric(12,2) not null,
  member_count_at_creation    int not null check (member_count_at_creation > 0),
  notes                       text,
  created_by                  uuid references users(id),
  created_at                  timestamptz not null default now(),
  unique (room_id, billing_month)
);

create index if not exists idx_electricity_readings_month on electricity_readings(billing_month);
create index if not exists idx_electricity_readings_room on electricity_readings(room_id);

notify pgrst, 'reload schema';
