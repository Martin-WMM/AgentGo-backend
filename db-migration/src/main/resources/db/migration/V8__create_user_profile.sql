CREATE TABLE IF NOT EXISTS tbl_user_profile (
    id UUID PRIMARY KEY,
    owner_user_id VARCHAR(255) NOT NULL,
    authentik_user_uuid UUID,
    username VARCHAR(150) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    email VARCHAR(254),
    phone_number VARCHAR(32),
    avatar_url VARCHAR(2048),
    description VARCHAR(2000),
    occupation VARCHAR(128),
    organization VARCHAR(255),
    homepage_url VARCHAR(2048),
    location VARCHAR(255),
    timezone VARCHAR(64),
    preferred_language VARCHAR(35),
    creator VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    CONSTRAINT uq_tbl_user_profile_owner UNIQUE (owner_user_id),
    CONSTRAINT uq_tbl_user_profile_username UNIQUE (username),
    CONSTRAINT uq_tbl_user_profile_authentik_uuid UNIQUE (authentik_user_uuid)
);

CREATE INDEX IF NOT EXISTS idx_tbl_user_profile_email
    ON tbl_user_profile (email);

COMMENT ON TABLE tbl_user_profile IS 'Authenticated user profile. Identity columns are mirrored from Authentik; extended columns are AgentGo-owned context for future agent access.';
COMMENT ON COLUMN tbl_user_profile.id IS 'Persistent profile identifier';
COMMENT ON COLUMN tbl_user_profile.owner_user_id IS 'OIDC subject of the authenticated user';
COMMENT ON COLUMN tbl_user_profile.authentik_user_uuid IS 'Authentik core user UUID';
COMMENT ON COLUMN tbl_user_profile.username IS 'Username mirrored from Authentik';
COMMENT ON COLUMN tbl_user_profile.display_name IS 'Display name mirrored from Authentik';
COMMENT ON COLUMN tbl_user_profile.email IS 'Email address mirrored from Authentik';
COMMENT ON COLUMN tbl_user_profile.phone_number IS 'Optional phone number maintained in AgentGo';
COMMENT ON COLUMN tbl_user_profile.avatar_url IS 'Avatar URL mirrored from Authentik';
COMMENT ON COLUMN tbl_user_profile.description IS 'Free-form personal description for agent context';
COMMENT ON COLUMN tbl_user_profile.occupation IS 'Occupation or job title for agent context';
COMMENT ON COLUMN tbl_user_profile.organization IS 'Organization or employer for agent context';
COMMENT ON COLUMN tbl_user_profile.homepage_url IS 'Personal or organization homepage URL';
COMMENT ON COLUMN tbl_user_profile.location IS 'Free-form location for agent context';
COMMENT ON COLUMN tbl_user_profile.timezone IS 'IANA time zone id such as Asia/Shanghai';
COMMENT ON COLUMN tbl_user_profile.preferred_language IS 'Preferred BCP 47 language tag such as zh-CN';
COMMENT ON COLUMN tbl_user_profile.creator IS 'Identifier of the actor that created the profile';
COMMENT ON COLUMN tbl_user_profile.created_at IS 'Profile creation timestamp';
COMMENT ON COLUMN tbl_user_profile.updated_at IS 'Profile last update timestamp';
COMMENT ON COLUMN tbl_user_profile.updated_by IS 'Identifier of the actor that last updated the profile';
