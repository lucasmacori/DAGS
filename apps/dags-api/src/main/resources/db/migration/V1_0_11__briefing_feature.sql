ALTER TABLE source
    ADD COLUMN user_id UUID REFERENCES app_user (user_id) ON DELETE CASCADE,
    ADD COLUMN article_content TEXT,
    ADD COLUMN summarized_at TIMESTAMP;

ALTER TABLE rss_source_item
    ADD COLUMN content TEXT,
    ADD COLUMN summarized_at TIMESTAMP;

CREATE INDEX idx_source_user_id ON source (user_id);
CREATE INDEX idx_source_user_summarized ON source (user_id, summarized_at);
CREATE INDEX idx_rss_source_item_user_summarized ON rss_source_item (user_id, summarized_at);

CREATE TABLE user_briefing_settings
(
    user_id        UUID PRIMARY KEY REFERENCES app_user (user_id) ON DELETE CASCADE,
    enabled        BOOLEAN     NOT NULL DEFAULT FALSE,
    frequency      VARCHAR(32) NOT NULL DEFAULT 'DAILY',
    generation_time VARCHAR(8),
    system_prompt  TEXT,
    created_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE briefing
(
    briefing_id   UUID PRIMARY KEY,
    user_id       UUID        NOT NULL REFERENCES app_user (user_id) ON DELETE CASCADE,
    content       TEXT        NOT NULL,
    article_count INTEGER     NOT NULL DEFAULT 0,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_briefing_user_id ON briefing (user_id, created_at DESC);
