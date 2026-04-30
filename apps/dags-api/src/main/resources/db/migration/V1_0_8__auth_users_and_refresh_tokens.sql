CREATE TABLE app_user
(
    user_id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX app_user_email_uidx
    ON app_user (email);

CREATE TABLE auth_refresh_token
(
    token_id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user (user_id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX auth_refresh_token_token_hash_uidx
    ON auth_refresh_token (token_hash);

CREATE INDEX auth_refresh_token_user_id_idx
    ON auth_refresh_token (user_id);
