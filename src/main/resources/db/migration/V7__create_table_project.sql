create table project (
  id                  uuid                     not null default uuidv7() primary key,
  created_at          timestamp with time zone not null default current_timestamp,
  created_by          varchar(64)              not null,
  updated_at          timestamp with time zone not null default current_timestamp,
  updated_by          varchar(64)              not null,
  version             integer                  not null default 0,
  key                 varchar(10)              not null,
  name                varchar(100)             not null,
  description         text,
  next_issue_number   integer                  not null default 1
);
