ALTER TABLE tbl_file
    ADD COLUMN IF NOT EXISTS creator VARCHAR(255) NOT NULL DEFAULT 'system',
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255) NOT NULL DEFAULT 'system';

COMMENT ON COLUMN tbl_file.creator IS 'Identifier of the actor that created the metadata';
COMMENT ON COLUMN tbl_file.updated_by IS 'Identifier of the actor that last updated the metadata';
