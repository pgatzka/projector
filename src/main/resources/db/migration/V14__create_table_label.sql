create table label (
  id          uuid                     not null default uuidv7() primary key,
  created_at  timestamp with time zone not null default current_timestamp,
  created_by  varchar(64)              not null,
  updated_at  timestamp with time zone not null default current_timestamp,
  updated_by  varchar(64)              not null,
  version     integer                  not null default 0,
  project_id  uuid                     not null references project(id) on delete cascade,
  name        varchar(50)              not null,
  color       label_color              not null
);
