create trigger label_set_updated_at
before update on label
for each row execute function set_updated_at();
