CREATE TABLE IF NOT EXISTS tbl_model (
    id UUID PRIMARY KEY,
    owner_user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    provider VARCHAR(255) NOT NULL,
    model_type VARCHAR(32) NOT NULL,
    configuration JSONB NOT NULL DEFAULT '{}'::jsonb,
    description VARCHAR(2000),
    creator VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    CONSTRAINT uq_tbl_model_owner_name UNIQUE (owner_user_id, name),
    CONSTRAINT chk_tbl_model_type CHECK (
        model_type IN (
            'LLM',
            'MULTIMODAL',
            'EMBEDDING',
            'ASR',
            'TTS',
            'SPEECH2SPEECH',
            'OTHER'
        )
    )
);

CREATE INDEX IF NOT EXISTS idx_tbl_model_owner_type
    ON tbl_model (owner_user_id, model_type);

CREATE INDEX IF NOT EXISTS idx_tbl_model_owner_provider
    ON tbl_model (owner_user_id, provider);

COMMENT ON TABLE tbl_model IS 'User-owned AI model configurations';
COMMENT ON COLUMN tbl_model.id IS 'Persistent model identifier';
COMMENT ON COLUMN tbl_model.owner_user_id IS 'Authenticated owner resolved from the security context';
COMMENT ON COLUMN tbl_model.name IS 'Human-readable model name unique per owner';
COMMENT ON COLUMN tbl_model.provider IS 'Upstream provider identifier';
COMMENT ON COLUMN tbl_model.model_type IS 'Model capability category';
COMMENT ON COLUMN tbl_model.configuration IS 'Provider-specific JSON configuration';
COMMENT ON COLUMN tbl_model.description IS 'Optional free-form description';
COMMENT ON COLUMN tbl_model.creator IS 'Identifier of the actor that created the model';
COMMENT ON COLUMN tbl_model.created_at IS 'Model creation timestamp';
COMMENT ON COLUMN tbl_model.updated_at IS 'Model last update timestamp';
COMMENT ON COLUMN tbl_model.updated_by IS 'Identifier of the actor that last updated the model';
