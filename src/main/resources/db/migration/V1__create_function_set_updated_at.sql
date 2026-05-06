create or replace function set_updated_at()
returns trigger
language plpgsql as $$
begin
  if (to_jsonb(new) - 'updated_at' - 'updated_by' - 'version')
     is distinct from
     (to_jsonb(old) - 'updated_at' - 'updated_by' - 'version')
  then
    new.updated_at := current_timestamp;
  end if;
  return new;
end
$$;
