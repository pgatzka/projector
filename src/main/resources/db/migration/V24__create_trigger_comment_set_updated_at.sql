create trigger comment_set_updated_at
before update on comment
for each row execute function set_updated_at();
