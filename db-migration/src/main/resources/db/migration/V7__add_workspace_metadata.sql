ALTER TABLE tbl_workspace
    ADD COLUMN IF NOT EXISTS metadata JSONB NOT NULL DEFAULT '{"color":"#1D4ED8"}'::jsonb;

COMMENT ON COLUMN tbl_workspace.metadata IS 'Presentation metadata. color is a supported #RRGGBB workspace accent.';
