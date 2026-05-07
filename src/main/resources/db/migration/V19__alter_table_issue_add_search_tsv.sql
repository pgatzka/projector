alter table issue
add column search_tsv tsvector
generated always as (
  to_tsvector('english',
    coalesce(title, '') || ' ' || coalesce(description_md, '')
  )
) stored;
