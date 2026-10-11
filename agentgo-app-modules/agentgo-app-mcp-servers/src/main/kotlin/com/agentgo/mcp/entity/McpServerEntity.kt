package com.agentgo.mcp.entity

import com.agentgo.dto.mcp.McpAuthType
import com.agentgo.dto.mcp.McpServerStatus
import com.agentgo.dto.mcp.McpTransport
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * Persistent representation of a user-owned MCP Streamable HTTP server.
 *
 * Ownership is enforced through [ownerUserId], which is always derived from the
 * authenticated security principal and never accepted from client request bodies.
 * Local/stdio transports are intentionally unsupported.
 */
@Entity
@Table(name = "tbl_mcp_server")
@EntityListeners(AuditingEntityListener::class)
class McpServerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "owner_user_id", nullable = false, updatable = false, length = 255)
    var ownerUserId: String = "",

    @Column(name = "name", nullable = false, length = 255)
    var name: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "transport", nullable = false, length = 32)
    var transport: McpTransport = McpTransport.STREAMABLE_HTTP,

    @Column(name = "endpoint_url", nullable = false, length = 2048)
    var endpointUrl: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false, length = 32)
    var authType: McpAuthType = McpAuthType.NONE,

    @Column(name = "auth_token", length = 4000)
    var authToken: String? = null,

    @Column(name = "auth_header_name", length = 255)
    var authHeaderName: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_headers", nullable = false)
    var customHeaders: Map<String, String> = emptyMap(),

    @Column(name = "protocol_version", length = 32)
    var protocolVersion: String? = null,

    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: McpServerStatus = McpServerStatus.UNKNOWN,

    @Column(name = "remote_server_name", length = 255)
    var remoteServerName: String? = null,

    @Column(name = "remote_server_version", length = 255)
    var remoteServerVersion: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "server_capabilities", nullable = false)
    var serverCapabilities: Map<String, Any?> = emptyMap(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tools_cache", nullable = false)
    var toolsCache: List<Map<String, Any?>> = emptyList(),

    @Column(name = "last_connected_at")
    var lastConnectedAt: Instant? = null,

    @Column(name = "last_error", length = 2000)
    var lastError: String? = null,

    @Column(name = "description", length = 2000)
    var description: String? = null,

    @CreatedBy
    @Column(name = "creator", nullable = false, updatable = false, length = 255)
    var creator: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null,

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 255)
    var updatedBy: String? = null,
)
