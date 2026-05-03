ALTER TABLE conversation_message
ADD COLUMN sources_json jsonb NOT NULL DEFAULT '[]'::jsonb;
