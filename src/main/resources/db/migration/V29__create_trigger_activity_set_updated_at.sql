create trigger activity_set_updated_at
before update on activity
for each row execute function set_updated_at();
