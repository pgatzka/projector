alter table comment
add column search_tsv tsvector
generated always as (to_tsvector('english', coalesce(body_md, ''))) stored;
