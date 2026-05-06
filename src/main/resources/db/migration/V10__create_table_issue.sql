create table issue (
  id                uuid                     not null default uuidv7() primary key,
  created_at        timestamp with time zone not null default current_timestamp,
  created_by        varchar(64)              not null,
  updated_at        timestamp with time zone not null default current_timestamp,
  updated_by        varchar(64)              not null,
  version           integer                  not null default 0,
  project_id        uuid                     not null references project(id) on delete cascade,
  number            integer                  not null,
  title             varchar(200)             not null,
  description_md    text,
  status            issue_status             not null default 'todo',
  priority          issue_priority           not null default 'medium',
  due_date          date
);
