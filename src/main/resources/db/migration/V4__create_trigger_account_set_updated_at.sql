create trigger account_set_updated_at
before update on account
for each row execute function set_updated_at();
