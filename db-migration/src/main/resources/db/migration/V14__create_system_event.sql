CREATE TABLE IF NOT EXISTS tbl_system_event (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    domain VARCHAR(255) NOT NULL,
    operator_name VARCHAR(255) NOT NULL,
    comment TEXT,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    creator VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tbl_system_event_occurred_at_id
    ON tbl_system_event (occurred_at, id);

CREATE INDEX IF NOT EXISTS idx_tbl_system_event_domain
    ON tbl_system_event (domain);

COMMENT ON TABLE tbl_system_event IS 'Global system events. Retention keeps the newest 1000 rows from the last three months.';
COMMENT ON COLUMN tbl_system_event.id IS 'Persistent system event identifier';
COMMENT ON COLUMN tbl_system_event.name IS 'Human-readable event name';
COMMENT ON COLUMN tbl_system_event.domain IS 'Business domain that produced the event';
COMMENT ON COLUMN tbl_system_event.operator_name IS 'Person related to the event';
COMMENT ON COLUMN tbl_system_event.comment IS 'Optional free-form comment';
COMMENT ON COLUMN tbl_system_event.occurred_at IS 'Instant when the event happened';
COMMENT ON COLUMN tbl_system_event.creator IS 'Identifier of the actor that created the row';
COMMENT ON COLUMN tbl_system_event.created_at IS 'Row creation timestamp';
COMMENT ON COLUMN tbl_system_event.updated_at IS 'Row last update timestamp';
COMMENT ON COLUMN tbl_system_event.updated_by IS 'Identifier of the actor that last updated the row';
