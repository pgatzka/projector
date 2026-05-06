create trigger issue_set_updated_at
before update on issue
for each row execute function set_updated_at();
