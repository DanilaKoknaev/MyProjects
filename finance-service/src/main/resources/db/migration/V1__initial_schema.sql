create table departments (
    id uuid primary key, version bigint not null, code varchar(50) not null unique, name varchar(200) not null
);

create table users (
    id uuid primary key, version bigint not null, email varchar(320) not null unique,
    password_hash varchar(255), full_name varchar(200) not null, role varchar(30) not null,
    department_id uuid references departments(id), enabled boolean not null default true
);

alter table departments add column responsible_user_id uuid references users(id);

create table accounts (
    id uuid primary key, version bigint not null, name varchar(200) not null, type varchar(30) not null,
    currency varchar(3) not null, department_id uuid not null references departments(id), active boolean not null default true
);

create table categories (
    id uuid primary key, version bigint not null, name varchar(200) not null, type varchar(20) not null,
    parent_id uuid references categories(id), active boolean not null default true
);

create table counterparties (
    id uuid primary key, version bigint not null, name varchar(250) not null,
    tax_identifier varchar(50) unique, type varchar(20) not null, active boolean not null default true
);

create table financial_transactions (
    id uuid primary key, version bigint not null, type varchar(20) not null, status varchar(20) not null,
    amount numeric(19,4) not null check (amount > 0), currency varchar(3) not null,
    operation_date date not null, department_id uuid not null references departments(id),
    category_id uuid references categories(id), counterparty_id uuid references counterparties(id),
    author_id uuid not null references users(id), reversal_of_id uuid unique references financial_transactions(id),
    created_at timestamptz not null, confirmed_at timestamptz
);

create table transaction_entries (
    id uuid primary key, version bigint not null, transaction_id uuid not null references financial_transactions(id),
    account_id uuid not null references accounts(id), amount numeric(19,4) not null check (amount <> 0)
);

create table budgets (
    id uuid primary key, version bigint not null, department_id uuid not null references departments(id),
    category_id uuid not null references categories(id), period_type varchar(10) not null,
    period_start date not null, period_end date not null, planned_amount numeric(19,4) not null check (planned_amount > 0),
    currency varchar(3) not null, unique(department_id, category_id, period_start, period_end)
);

create table refresh_tokens (
    id uuid primary key, version bigint not null, token_hash varchar(64) not null unique,
    user_id uuid not null references users(id), expires_at timestamptz not null, revoked boolean not null default false
);

create table audit_events (
    id uuid primary key, version bigint not null, actor_id uuid, transaction_id uuid,
    action varchar(30) not null, result varchar(20) not null, details text, occurred_at timestamptz not null
);

create index idx_users_department on users(department_id);
create index idx_accounts_department on accounts(department_id);
create index idx_transactions_department_date on financial_transactions(department_id, operation_date);
create index idx_entries_account on transaction_entries(account_id);
create index idx_audit_occurred_at on audit_events(occurred_at desc);
