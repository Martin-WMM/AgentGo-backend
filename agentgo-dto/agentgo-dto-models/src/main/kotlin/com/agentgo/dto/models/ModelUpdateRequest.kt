package com.agentgo.dto.models

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Request body used to replace fields of an existing user-owned model.
 *
 * @property name human-readable display name unique per owner
 * @property provider upstream provider identifier such as `openai` or `ollama`
 * @property type model capability category
 * @property configuration provider-specific settings stored as a JSON object
 * @property description optional free-form description
 */
@Schema(description = "Request body for updating a user-owned model configuration")
data class ModelUpdateRequest(
    @field:Schema(
        description = "Human-readable model name unique for the authenticated user",
        example = "gpt-4o-mini",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 255,
    )
    val name: String,
    @field:Schema(
        description = "Upstream model provider identifier",
        example = "openai",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 255,
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
        example = "{\"baseUrl\":\"https://api.openai.com/v1\",\"model\":\"gpt-4o-mini\"}",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val configuration: Map<String, Any?> = emptyMap(),
    @field:Schema(
        description = "Optional free-form description of the model",
        example = "Default chat model for the workspace",
        maxLength = 2000,
    )
    val description: String? = null,
)
