ALTER TABLE rss_source_item
    DROP CONSTRAINT rss_source_item_source_id_fkey;

ALTER TABLE rss_source_item
    ADD CONSTRAINT rss_source_item_source_id_fkey
        FOREIGN KEY (source_id)
            REFERENCES source (source_id)
            ON DELETE CASCADE;
