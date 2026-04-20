CREATE TABLE source
(
    source_id  UUID PRIMARY KEY,
    type       VARCHAR(64)  NOT NULL,
    title      VARCHAR(255),
    content    TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
