create type activity_action as enum (
  'issue_created',
  'status_changed',
  'priority_changed',
  'due_date_changed',
  'title_edited',
  'description_edited',
  'label_added',
  'label_removed'
);
