ALTER TABLE tbl_file
    DROP COLUMN IF EXISTS owner_id,
    DROP COLUMN IF EXISTS category,
    DROP COLUMN IF EXISTS original_filename,
    ADD COLUMN IF NOT EXISTS version_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS metadata JSONB NOT NULL DEFAULT '{}'::jsonb;

ALTER TABLE tbl_file
    DROP CONSTRAINT IF EXISTS chk_tbl_file_category;

DROP INDEX IF EXISTS idx_tbl_file_owner_category;

COMMENT ON TABLE tbl_file IS 'MinIO object metadata';
COMMENT ON COLUMN tbl_file.id IS 'Application metadata identifier';
COMMENT ON COLUMN tbl_file.bucket_name IS 'MinIO bucket name';
COMMENT ON COLUMN tbl_file.object_key IS 'MinIO object key';
COMMENT ON COLUMN tbl_file.content_type IS 'MinIO object content type';
COMMENT ON COLUMN tbl_file.size_bytes IS 'MinIO object size in bytes';
COMMENT ON COLUMN tbl_file.etag IS 'MinIO object entity tag';
COMMENT ON COLUMN tbl_file.version_id IS 'MinIO object version identifier';
COMMENT ON COLUMN tbl_file.metadata IS 'MinIO user metadata';
COMMENT ON COLUMN tbl_file.created_at IS 'Metadata creation timestamp';
COMMENT ON COLUMN tbl_file.updated_at IS 'Metadata last update timestamp';
