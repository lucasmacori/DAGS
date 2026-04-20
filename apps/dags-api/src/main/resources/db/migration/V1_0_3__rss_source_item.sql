CREATE TABLE rss_source_item
(
    rss_source_item_id UUID PRIMARY KEY,
    source_id          UUID         NOT NULL REFERENCES source (source_id),
    user_id            VARCHAR(255) NOT NULL,
    external_id        VARCHAR(1024) NOT NULL,
    title              VARCHAR(512),
    link               TEXT,
    published_at       TIMESTAMP,
    discovered_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_rss_source_item_source_user_external
    ON rss_source_item (source_id, user_id, external_id);
