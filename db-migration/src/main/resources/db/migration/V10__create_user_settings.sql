CREATE TABLE IF NOT EXISTS tbl_user_settings (
    id UUID PRIMARY KEY,
    owner_user_id VARCHAR(255) NOT NULL,
    theme VARCHAR(16) NOT NULL,
    locale VARCHAR(16) NOT NULL,
    sidebar_collapsed BOOLEAN NOT NULL,
    density VARCHAR(16) NOT NULL,
    reduce_motion BOOLEAN NOT NULL,
    creator VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    CONSTRAINT uq_tbl_user_settings_owner UNIQUE (owner_user_id),
    CONSTRAINT chk_tbl_user_settings_theme CHECK (theme IN ('LIGHT', 'DARK', 'SYSTEM')),
    CONSTRAINT chk_tbl_user_settings_locale CHECK (locale IN ('EN', 'ZH_CN')),
    CONSTRAINT chk_tbl_user_settings_density CHECK (density IN ('COMFORTABLE', 'COMPACT'))
);

COMMENT ON TABLE tbl_user_settings IS 'Per-user system settings applied by AgentGo clients.';
COMMENT ON COLUMN tbl_user_settings.id IS 'Persistent settings identifier';
COMMENT ON COLUMN tbl_user_settings.owner_user_id IS 'OIDC subject of the authenticated user';
COMMENT ON COLUMN tbl_user_settings.theme IS 'Color theme: LIGHT, DARK, or SYSTEM';
COMMENT ON COLUMN tbl_user_settings.locale IS 'Interface language: EN or ZH_CN';
COMMENT ON COLUMN tbl_user_settings.sidebar_collapsed IS 'Whether the workspace sidebar is collapsed';
COMMENT ON COLUMN tbl_user_settings.density IS 'Information density: COMFORTABLE or COMPACT';
COMMENT ON COLUMN tbl_user_settings.reduce_motion IS 'Whether animations and transitions are minimized';
COMMENT ON COLUMN tbl_user_settings.creator IS 'Identifier of the actor that created the settings';
COMMENT ON COLUMN tbl_user_settings.created_at IS 'Settings creation timestamp';
COMMENT ON COLUMN tbl_user_settings.updated_at IS 'Settings last update timestamp';
COMMENT ON COLUMN tbl_user_settings.updated_by IS 'Identifier of the actor that last updated the settings';
