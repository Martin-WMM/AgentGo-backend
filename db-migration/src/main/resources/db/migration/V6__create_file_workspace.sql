CREATE TABLE IF NOT EXISTS tbl_workspace (
    id UUID PRIMARY KEY,
    owner_user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    creator VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    CONSTRAINT uq_tbl_workspace_owner_name UNIQUE (owner_user_id, name)
);

CREATE INDEX IF NOT EXISTS idx_tbl_workspace_owner_updated
    ON tbl_workspace (owner_user_id, updated_at DESC);

COMMENT ON TABLE tbl_workspace IS 'User-owned file workspaces';
COMMENT ON COLUMN tbl_workspace.id IS 'Persistent workspace identifier';
COMMENT ON COLUMN tbl_workspace.owner_user_id IS 'Authenticated owner resolved from the security context';
COMMENT ON COLUMN tbl_workspace.name IS 'Human-readable workspace name unique per owner';
COMMENT ON COLUMN tbl_workspace.description IS 'Optional free-form description';
COMMENT ON COLUMN tbl_workspace.creator IS 'Identifier of the actor that created the workspace';
COMMENT ON COLUMN tbl_workspace.created_at IS 'Workspace creation timestamp';
COMMENT ON COLUMN tbl_workspace.updated_at IS 'Workspace last update timestamp';
COMMENT ON COLUMN tbl_workspace.updated_by IS 'Identifier of the actor that last updated the workspace';
