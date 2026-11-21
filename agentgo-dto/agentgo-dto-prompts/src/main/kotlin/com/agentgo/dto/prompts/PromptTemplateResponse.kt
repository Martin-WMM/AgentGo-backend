package com.agentgo.dto.prompts

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * API representation of a user-owned prompt template.
 *
 * @property id persistent template identifier
 * @property groupName group that scopes the template name
 * @property name display name
 * @property version live version label, always `latest`
 * @property revision head revision number that `latest` points at
 * @property engine rendering engine
 * @property content template body
 * @property description optional free-form description
 * @property tags display tags
 * @property metadata presentation color and icon
 * @property createdAt creation timestamp
 * @property updatedAt last modification timestamp
 */
@Schema(description = "User-owned prompt template returned by GET, POST, and PUT /api/v1/promptTemplates")
data class PromptTemplateResponse(
    @field:Schema(
        description = "Persistent prompt template identifier",
        format = "int64",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val id: Long,
    @field:Schema(
        description = "Group name. Unique together with name and the creator.",
        example = "chat",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val groupName: String,
    @field:Schema(
        description = "Human-readable template name, unique with groupName and the creator",
        example = "Default system",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val name: String,
    @field:Schema(
        description = "Live version label. Always latest. Concrete commits are listed under /revisions.",
        example = "latest",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val version: String,
    @field:Schema(
        description = "Head revision number that latest points at",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val revision: Int,
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
    @field:Schema(description = "Display tags", requiredMode = Schema.RequiredMode.REQUIRED)
    val tags: List<String> = emptyList(),
    @field:Schema(description = "Presentation metadata, including the accent color and optional icon")
    val metadata: PromptTemplateMetadata = PromptTemplateMetadata(),
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
