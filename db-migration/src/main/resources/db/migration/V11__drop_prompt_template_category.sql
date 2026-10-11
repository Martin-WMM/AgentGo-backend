ALTER TABLE tbl_prompt_template
    DROP CONSTRAINT IF EXISTS uq_tbl_prompt_template_owner_category_name;

ALTER TABLE tbl_prompt_template
    DROP CONSTRAINT IF EXISTS chk_tbl_prompt_template_category;

DROP INDEX IF EXISTS idx_tbl_prompt_template_owner_category;

ALTER TABLE tbl_prompt_template
    DROP COLUMN IF EXISTS category;

ALTER TABLE tbl_prompt_template
    ADD CONSTRAINT uq_tbl_prompt_template_owner_name UNIQUE (owner_user_id, name);

CREATE INDEX IF NOT EXISTS idx_tbl_prompt_template_owner_user_id
    ON tbl_prompt_template (owner_user_id);

COMMENT ON COLUMN tbl_prompt_template.name IS 'Human-readable template name unique per owner';
