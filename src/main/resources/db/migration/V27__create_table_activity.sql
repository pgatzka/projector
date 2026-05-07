create table activity (
  id          uuid                     not null default uuidv7() primary key,
  created_at  timestamp with time zone not null default current_timestamp,
  created_by  varchar(64)              not null,
  updated_at  timestamp with time zone not null default current_timestamp,
  updated_by  varchar(64)              not null,
  version     integer                  not null default 0,
  issue_id    uuid                     not null references issue(id) on delete cascade,
  action      activity_action          not null,
  payload     jsonb                    not null default '{}'::jsonb
);
