ALTER TABLE source
    ADD COLUMN article_read_at TIMESTAMP;

ALTER TABLE rss_source_item
    ADD COLUMN article_read_at TIMESTAMP;
