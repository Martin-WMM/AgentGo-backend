CREATE TABLE IF NOT EXISTS tbl_file (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    bucket_name VARCHAR(255) NOT NULL,
    object_key VARCHAR(1024) NOT NULL,
    category VARCHAR(32) NOT NULL,
    original_filename VARCHAR(512) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,
    etag VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_tbl_file_bucket_object UNIQUE (bucket_name, object_key),
    CONSTRAINT chk_tbl_file_category CHECK (category IN ('AVATAR', 'WORKSPACE')),
    CONSTRAINT chk_tbl_file_size CHECK (size_bytes >= 0)
);

CREATE INDEX IF NOT EXISTS idx_tbl_file_owner_category
    ON tbl_file (owner_id, category);
