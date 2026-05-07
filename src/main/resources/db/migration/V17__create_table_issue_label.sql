create table issue_label (
  id          uuid                     not null default uuidv7() primary key,
  created_at  timestamp with time zone not null default current_timestamp,
  created_by  varchar(64)              not null,
  updated_at  timestamp with time zone not null default current_timestamp,
  updated_by  varchar(64)              not null,
  version     integer                  not null default 0,
  issue_id    uuid                     not null references issue(id) on delete cascade,
  label_id    uuid                     not null references label(id) on delete cascade,
  unique (issue_id, label_id)
);
