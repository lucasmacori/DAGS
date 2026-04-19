CREATE TABLE conversation_message
(
    message_id      UUID PRIMARY KEY,
    conversation_id UUID         NOT NULL REFERENCES conversation_entity (conversation_id),
    role            VARCHAR(32)  NOT NULL,
    content         TEXT         NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_conversation_message_conversation_created_at
    ON conversation_message (conversation_id, created_at DESC, message_id DESC);
