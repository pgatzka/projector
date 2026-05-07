create trigger issue_label_set_updated_at
before update on issue_label
for each row execute function set_updated_at();
