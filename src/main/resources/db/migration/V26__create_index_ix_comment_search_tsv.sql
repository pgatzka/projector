create index ix_comment_search_tsv on comment using gin (search_tsv);
