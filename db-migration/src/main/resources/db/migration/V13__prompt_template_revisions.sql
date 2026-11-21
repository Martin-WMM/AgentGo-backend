ALTER TABLE tbl_prompt_template
    ADD COLUMN IF NOT EXISTS group_name VARCHAR(255);

UPDATE tbl_prompt_template
SET group_name = 'default'
WHERE group_name IS NULL OR btrim(group_name) = '';

ALTER TABLE tbl_prompt_template
    ALTER COLUMN group_name SET NOT NULL;

ALTER TABLE tbl_prompt_template
    ADD COLUMN IF NOT EXISTS version VARCHAR(32) NOT NULL DEFAULT 'latest';

ALTER TABLE tbl_prompt_template
    ADD COLUMN IF NOT EXISTS head_revision INTEGER NOT NULL DEFAULT 1;

ALTER TABLE tbl_prompt_template
    ADD COLUMN IF NOT EXISTS tags JSONB NOT NULL DEFAULT '[]'::jsonb;

ALTER TABLE tbl_prompt_template
    ADD COLUMN IF NOT EXISTS metadata JSONB NOT NULL DEFAULT '{"color":"#1D4ED8"}'::jsonb;

ALTER TABLE tbl_prompt_template
    DROP CONSTRAINT IF EXISTS uq_tbl_prompt_template_owner_name;

ALTER TABLE tbl_prompt_template
    DROP CONSTRAINT IF EXISTS uq_tbl_prompt_template_group_name_name_creator;

ALTER TABLE tbl_prompt_template
    ADD CONSTRAINT uq_tbl_prompt_template_group_name_name_creator
        UNIQUE (group_name, name, creator);

ALTER TABLE tbl_prompt_template
    DROP CONSTRAINT IF EXISTS chk_tbl_prompt_template_version;

ALTER TABLE tbl_prompt_template
    ADD CONSTRAINT chk_tbl_prompt_template_version CHECK (version = 'latest');

ALTER TABLE tbl_prompt_template
    DROP CONSTRAINT IF EXISTS chk_tbl_prompt_template_head_revision;

ALTER TABLE tbl_prompt_template
    ADD CONSTRAINT chk_tbl_prompt_template_head_revision CHECK (head_revision >= 1);

COMMENT ON COLUMN tbl_prompt_template.group_name IS 'Group name unique together with name and creator';
COMMENT ON COLUMN tbl_prompt_template.name IS 'Human-readable template name unique with group name and creator';
COMMENT ON COLUMN tbl_prompt_template.version IS 'Live version label. Always latest';
COMMENT ON COLUMN tbl_prompt_template.head_revision IS 'Revision number that the latest version points at';
COMMENT ON COLUMN tbl_prompt_template.tags IS 'Display tags stored as a JSON array';
COMMENT ON COLUMN tbl_prompt_template.metadata IS 'Presentation metadata such as the accent color and icon';

CREATE TABLE IF NOT EXISTS tbl_prompt_template_revision (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL,
    revision_number INTEGER NOT NULL,
    parent_revision_number INTEGER,
    message VARCHAR(2000) NOT NULL,
    author VARCHAR(255) NOT NULL,
    diff TEXT NOT NULL,
    group_name VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    engine VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    description VARCHAR(2000),
    tags JSONB NOT NULL DEFAULT '[]'::jsonb,
    metadata JSONB NOT NULL DEFAULT '{"color":"#1D4ED8"}'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_tbl_prompt_template_revision_template_revision UNIQUE (template_id, revision_number),
    CONSTRAINT fk_tbl_prompt_template_revision_template
        FOREIGN KEY (template_id) REFERENCES tbl_prompt_template (id) ON DELETE CASCADE,
    CONSTRAINT chk_tbl_prompt_template_revision_engine CHECK (engine IN ('SIMPLE', 'JINJA2')),
    CONSTRAINT chk_tbl_prompt_template_revision_number CHECK (revision_number >= 1)
);

CREATE INDEX IF NOT EXISTS idx_tbl_prompt_template_revision_template_id
    ON tbl_prompt_template_revision (template_id, revision_number DESC);

COMMENT ON TABLE tbl_prompt_template_revision IS 'Git-style commits for user-owned prompt templates';
COMMENT ON COLUMN tbl_prompt_template_revision.id IS 'Persistent revision identifier';
COMMENT ON COLUMN tbl_prompt_template_revision.template_id IS 'Owning prompt template identifier';
COMMENT ON COLUMN tbl_prompt_template_revision.revision_number IS 'One-based revision number';
COMMENT ON COLUMN tbl_prompt_template_revision.parent_revision_number IS 'Previous revision number, null on the first commit';
COMMENT ON COLUMN tbl_prompt_template_revision.message IS 'Commit comment';
COMMENT ON COLUMN tbl_prompt_template_revision.author IS 'Authenticated principal that recorded the commit';
COMMENT ON COLUMN tbl_prompt_template_revision.diff IS 'Unified diff of metadata and content';
COMMENT ON COLUMN tbl_prompt_template_revision.group_name IS 'Group name after this commit';
COMMENT ON COLUMN tbl_prompt_template_revision.name IS 'Template name after this commit';
COMMENT ON COLUMN tbl_prompt_template_revision.engine IS 'Template engine after this commit';
COMMENT ON COLUMN tbl_prompt_template_revision.content IS 'Template body after this commit';
COMMENT ON COLUMN tbl_prompt_template_revision.description IS 'Description after this commit';
COMMENT ON COLUMN tbl_prompt_template_revision.tags IS 'Tags after this commit';
COMMENT ON COLUMN tbl_prompt_template_revision.metadata IS 'Presentation metadata after this commit';
COMMENT ON COLUMN tbl_prompt_template_revision.created_at IS 'Commit timestamp';

INSERT INTO tbl_prompt_template_revision (
    id,
    template_id,
    revision_number,
    parent_revision_number,
    message,
    author,
    diff,
    group_name,
    name,
    engine,
    content,
    description,
    tags,
    metadata,
    created_at
)
SELECT
    gen_random_uuid(),
    template.id,
    1,
    NULL,
    'Initial commit',
    template.creator,
    E'diff --git a/metadata b/metadata\n'
        || E'new file mode 100644\n'
        || E'--- /dev/null\n'
        || E'+++ b/metadata\n'
        || E'@@ -0,0 +1,8 @@\n'
        || '+groupName: ' || template.group_name || E'\n'
        || '+name: ' || template.name || E'\n'
        || '+engine: ' || template.engine || E'\n'
        || '+description: ' || coalesce(template.description, '') || E'\n'
        || '+color: ' || coalesce(template.metadata ->> 'color', '#1D4ED8') || E'\n'
        || '+icon.source: ' || coalesce(template.metadata #>> '{icon,source}', '') || E'\n'
        || '+icon.value: ' || coalesce(template.metadata #>> '{icon,value}', '') || E'\n'
        || '+tags: ' || coalesce(
            (
                SELECT string_agg(tag.value, ', ')
                FROM jsonb_array_elements_text(template.tags) AS tag(value)
            ),
            ''
        ) || E'\n'
        || E'diff --git a/content b/content\n'
        || E'new file mode 100644\n'
        || E'--- /dev/null\n'
        || E'+++ b/content\n'
        || '@@ -0,0 +1,'
        || (length(template.content) - length(replace(template.content, E'\n', '')) + 1)::text
        || E' @@\n+'
        || replace(template.content, E'\n', E'\n+'),
    template.group_name,
    template.name,
    template.engine,
    template.content,
    template.description,
    template.tags,
    template.metadata,
    template.created_at
FROM tbl_prompt_template AS template
WHERE NOT EXISTS (
    SELECT 1
    FROM tbl_prompt_template_revision AS revision
    WHERE revision.template_id = template.id
);
