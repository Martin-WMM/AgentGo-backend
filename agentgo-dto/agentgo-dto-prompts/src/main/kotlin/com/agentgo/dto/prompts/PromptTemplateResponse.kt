package com.agentgo.dto.prompts

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

/**
 * API representation of a user-owned prompt template.
 *
 * @property id persistent template identifier
 * @property name display name
 * @property engine rendering engine
 * @property content template body
 * @property description optional free-form description
 * @property createdAt creation timestamp
 * @property updatedAt last modification timestamp
 */
@Schema(description = "User-owned prompt template returned by GET, POST, and PUT /api/v1/promptTemplates")
data class PromptTemplateResponse(
    @field:Schema(
        description = "Persistent prompt template identifier",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val id: UUID,
    @field:Schema(
        description = "Human-readable template name unique for the authenticated user",
        example = "Default system",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val name: String,
    @field:Schema(
        description = "Template engine",
        example = "SIMPLE",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val engine: PromptEngine,
    @field:Schema(
        description = "Template body",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val content: String,
    @field:Schema(description = "Optional free-form description")
    val description: String? = null,
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
