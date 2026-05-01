DO $$
BEGIN
    IF to_regclass('public.chat_document_vector_store') IS NOT NULL THEN
        TRUNCATE TABLE public.chat_document_vector_store;
        ALTER TABLE public.chat_document_vector_store
            ALTER COLUMN embedding TYPE vector(768);
    END IF;
END $$;
