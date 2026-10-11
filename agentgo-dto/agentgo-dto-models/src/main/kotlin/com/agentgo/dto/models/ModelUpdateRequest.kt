package com.agentgo.dto.models

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Request body used to replace fields of an existing user-owned model.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 *
 * @property name human-readable display name unique per owner
 * @property provider upstream provider identifier such as `openai` or `ollama`
 * @property type model capability category
 * @property configuration provider-specific settings stored as a JSON object
 * @property description optional free-form description
 */
@Schema(description = "Request body for updating a user-owned model configuration")
class ModelUpdateRequest {
    @field:Schema(
        description = "Human-readable model name unique for the authenticated user",
        example = "gpt-4o-mini",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 255,
    )
    var name: String = ""

    @field:Schema(
        description = "Upstream model provider identifier",
        example = "openai",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 255,
    )
    var provider: String = ""

    @field:Schema(
        description = "Model capability category",
        example = "LLM",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var type: ModelType? = null

    @field:Schema(
        description = "Provider-specific configuration stored as a JSON object",
        example = "{\"baseUrl\":\"https://api.openai.com/v1\",\"model\":\"gpt-4o-mini\"}",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var configuration: Map<String, Any?> = emptyMap()

    @field:Schema(
        description = "Optional free-form description of the model",
        example = "Default chat model for the workspace",
        maxLength = 2000,
    )
    var description: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModelUpdateRequest) return false
        return name == other.name &&
            provider == other.provider &&
            type == other.type &&
            configuration == other.configuration &&
            description == other.description
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + provider.hashCode()
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + configuration.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }
}
