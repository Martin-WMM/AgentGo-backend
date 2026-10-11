package com.agentgo.dto.workspace

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

/**
 * API representation of a user-owned file workspace.
 *
 * @property id persistent workspace identifier
 * @property name human-readable display name
 * @property description optional free-form description
 * @property metadata presentation metadata, including the accent color
 * @property createdAt creation timestamp
 * @property updatedAt last modification timestamp
 */
@Schema(
    description = "User-owned file workspace returned by /api/v1/workspaces endpoints inside " +
        "CommonHttpResponse.data (or as list elements for list operations).",
)
data class WorkspaceResponse(
    @field:Schema(
        description = "Persistent workspace identifier",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val id: UUID,
    @field:Schema(
        description = "Human-readable workspace name unique for the authenticated user",
        example = "Reports",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val name: String,
    @field:Schema(
        description = "Optional free-form description of the workspace",
        example = "Quarterly report drafts",
    )
    val description: String? = null,
    @field:Schema(description = "Presentation metadata. color is the workspace accent.")
    val metadata: WorkspaceMetadata = WorkspaceMetadata(),
    @field:Schema(
        description = "Creation timestamp",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val createdAt: Instant,
    @field:Schema(
        description = "Last modification timestamp",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val updatedAt: Instant,
)
