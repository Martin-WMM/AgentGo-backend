CREATE TABLE IF NOT EXISTS tbl_prompt_template (
    id UUID PRIMARY KEY,
    owner_user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(32) NOT NULL,
    engine VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    description VARCHAR(2000),
    creator VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    CONSTRAINT uq_tbl_prompt_template_owner_category_name UNIQUE (owner_user_id, category, name),
    CONSTRAINT chk_tbl_prompt_template_category CHECK (
        category IN ('SYSTEM', 'USER', 'AGENT', 'TOOL', 'OTHER')
    ),
    CONSTRAINT chk_tbl_prompt_template_engine CHECK (
        engine IN ('SIMPLE', 'JINJA2')
    )
);

CREATE INDEX IF NOT EXISTS idx_tbl_prompt_template_owner_category
    ON tbl_prompt_template (owner_user_id, category);

COMMENT ON TABLE tbl_prompt_template IS 'User-owned prompt templates';
COMMENT ON COLUMN tbl_prompt_template.id IS 'Persistent prompt template identifier';
COMMENT ON COLUMN tbl_prompt_template.owner_user_id IS 'Authenticated owner resolved from the security context';
COMMENT ON COLUMN tbl_prompt_template.name IS 'Human-readable template name unique per owner and category';
COMMENT ON COLUMN tbl_prompt_template.category IS 'Prompt template category';
COMMENT ON COLUMN tbl_prompt_template.engine IS 'Template engine used to render the content';
COMMENT ON COLUMN tbl_prompt_template.content IS 'Template body rendered by the selected engine';
COMMENT ON COLUMN tbl_prompt_template.description IS 'Optional free-form description';
COMMENT ON COLUMN tbl_prompt_template.creator IS 'Identifier of the actor that created the prompt template';
COMMENT ON COLUMN tbl_prompt_template.created_at IS 'Prompt template creation timestamp';
COMMENT ON COLUMN tbl_prompt_template.updated_at IS 'Prompt template last update timestamp';
COMMENT ON COLUMN tbl_prompt_template.updated_by IS 'Identifier of the actor that last updated the prompt template';
