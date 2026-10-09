package com.agentgo.dto.models

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

/**
 * API representation of a user-owned model configuration.
 *
 * @property id persistent model identifier
 * @property name human-readable display name
 * @property provider upstream provider identifier
 * @property type model capability category
 * @property configuration provider-specific settings
 * @property description optional free-form description
 * @property createdAt creation timestamp
 * @property updatedAt last modification timestamp
 */
@Schema(description = "User-owned model configuration returned by the models API")
data class ModelResponse(
    @field:Schema(
        description = "Persistent model identifier",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val id: UUID,
    @field:Schema(
        description = "Human-readable model name unique for the authenticated user",
        example = "gpt-4o-mini",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val name: String,
    @field:Schema(
        description = "Upstream model provider identifier",
        example = "openai",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val provider: String,
    @field:Schema(
        description = "Model capability category",
        example = "LLM",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val type: ModelType,
    @field:Schema(
        description = "Provider-specific configuration stored as a JSON object",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val configuration: Map<String, Any?>,
    @field:Schema(
        description = "Optional free-form description of the model",
        example = "Default chat model for the workspace",
    )
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
