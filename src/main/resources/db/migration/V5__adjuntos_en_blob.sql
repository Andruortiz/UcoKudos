ALTER TABLE kudo_attachment ADD COLUMN size_bytes bigint NOT NULL DEFAULT 0;
UPDATE kudo_attachment SET size_bytes = octet_length(content);
ALTER TABLE kudo_attachment ALTER COLUMN content DROP NOT NULL;
