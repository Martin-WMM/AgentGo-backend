CREATE TABLE IF NOT EXISTS tbl_mcp_server (
    id UUID PRIMARY KEY,
    owner_user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    transport VARCHAR(32) NOT NULL DEFAULT 'STREAMABLE_HTTP',
    endpoint_url VARCHAR(2048) NOT NULL,
    auth_type VARCHAR(32) NOT NULL DEFAULT 'NONE',
    auth_token VARCHAR(4000),
    auth_header_name VARCHAR(255),
    custom_headers JSONB NOT NULL DEFAULT '{}'::jsonb,
    protocol_version VARCHAR(32),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN',
    remote_server_name VARCHAR(255),
    remote_server_version VARCHAR(255),
    server_capabilities JSONB NOT NULL DEFAULT '{}'::jsonb,
    tools_cache JSONB NOT NULL DEFAULT '[]'::jsonb,
    last_connected_at TIMESTAMP WITH TIME ZONE,
    last_error VARCHAR(2000),
    description VARCHAR(2000),
    creator VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    CONSTRAINT uq_tbl_mcp_server_owner_name UNIQUE (owner_user_id, name),
    CONSTRAINT chk_tbl_mcp_server_transport CHECK (transport = 'STREAMABLE_HTTP'),
    CONSTRAINT chk_tbl_mcp_server_auth_type CHECK (
        auth_type IN ('NONE', 'BEARER', 'HEADER')
    ),
    CONSTRAINT chk_tbl_mcp_server_status CHECK (
        status IN ('UNKNOWN', 'CONNECTED', 'DISCONNECTED', 'ERROR')
    )
);

CREATE INDEX IF NOT EXISTS idx_tbl_mcp_server_owner_status
    ON tbl_mcp_server (owner_user_id, status);

CREATE INDEX IF NOT EXISTS idx_tbl_mcp_server_owner_enabled
    ON tbl_mcp_server (owner_user_id, enabled);

COMMENT ON TABLE tbl_mcp_server IS
    'User-owned MCP servers using Streamable HTTP transport only (no local/stdio mode)';
COMMENT ON COLUMN tbl_mcp_server.id IS 'Persistent MCP server identifier';
COMMENT ON COLUMN tbl_mcp_server.owner_user_id IS
    'Authenticated owner resolved from the security context';
COMMENT ON COLUMN tbl_mcp_server.name IS 'Human-readable display name unique per owner';
COMMENT ON COLUMN tbl_mcp_server.transport IS
    'MCP transport; AgentGo only supports STREAMABLE_HTTP';
COMMENT ON COLUMN tbl_mcp_server.endpoint_url IS
    'Single MCP Streamable HTTP endpoint URL (POST/GET)';
COMMENT ON COLUMN tbl_mcp_server.auth_type IS
    'Outbound auth applied to MCP HTTP requests: NONE, BEARER, or HEADER';
COMMENT ON COLUMN tbl_mcp_server.auth_token IS
    'Bearer token or custom header value used for MCP HTTP authentication';
COMMENT ON COLUMN tbl_mcp_server.auth_header_name IS
    'HTTP header name when auth_type is HEADER';
COMMENT ON COLUMN tbl_mcp_server.custom_headers IS
    'Additional HTTP headers sent with every MCP request';
COMMENT ON COLUMN tbl_mcp_server.protocol_version IS
    'Preferred or last-negotiated MCP protocol version (for example 2025-03-26)';
COMMENT ON COLUMN tbl_mcp_server.enabled IS
    'Whether the server is available for Agent runtime assembly';
COMMENT ON COLUMN tbl_mcp_server.status IS
    'Last known connectivity status from probe or runtime';
COMMENT ON COLUMN tbl_mcp_server.remote_server_name IS
    'serverInfo.name returned by MCP initialize';
COMMENT ON COLUMN tbl_mcp_server.remote_server_version IS
    'serverInfo.version returned by MCP initialize';
COMMENT ON COLUMN tbl_mcp_server.server_capabilities IS
    'Server capabilities object from MCP initialize';
COMMENT ON COLUMN tbl_mcp_server.tools_cache IS
    'Cached tools/list result for console display';
COMMENT ON COLUMN tbl_mcp_server.last_connected_at IS
    'Timestamp of the last successful initialize handshake';
COMMENT ON COLUMN tbl_mcp_server.last_error IS
    'Safe message from the last failed probe or runtime call';
COMMENT ON COLUMN tbl_mcp_server.description IS 'Optional free-form description';
COMMENT ON COLUMN tbl_mcp_server.creator IS 'Identifier of the actor that created the row';
COMMENT ON COLUMN tbl_mcp_server.created_at IS 'Row creation timestamp';
COMMENT ON COLUMN tbl_mcp_server.updated_at IS 'Row last update timestamp';
COMMENT ON COLUMN tbl_mcp_server.updated_by IS 'Identifier of the actor that last updated the row';
