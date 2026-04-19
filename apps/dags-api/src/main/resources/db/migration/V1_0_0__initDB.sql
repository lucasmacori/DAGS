CREATE TABLE conversation_entity
(
    conversation_id   UUID PRIMARY KEY,
    conversation_name VARCHAR(255) NOT NULL,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
