create trigger project_set_updated_at
before update on project
for each row execute function set_updated_at();
