create table account (
  id              uuid                     not null default uuidv7() primary key,
  created_at      timestamp with time zone not null default current_timestamp,
  created_by      varchar(64)              not null,
  updated_at      timestamp with time zone not null default current_timestamp,
  updated_by      varchar(64)              not null,
  version         integer                  not null default 0,
  email           varchar(254)             not null,
  password_hash   varchar(120)             not null,
  display_name    varchar(100)             not null,
  last_login_at   timestamp with time zone
);
