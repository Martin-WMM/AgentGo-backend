package com.agentgo.dto.prompts

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Request body used to replace fields of an existing user-owned prompt template.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 *
 * @property name display name unique for the owner
 * @property engine rendering engine
 * @property content template body
 * @property description optional free-form description
 */
@Schema(
    description = "Request body for PUT /api/v1/promptTemplates/{id}. Replaces name, engine, " +
        "content, and description. Uniqueness is checked against other templates owned by the " +
        "same user. The body is syntax-checked again.",
)
class PromptTemplateUpdateRequest {
    @field:Schema(
        description = "Human-readable template name unique for the authenticated user",
        example = "Default system",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 255,
    )
    var name: String = ""

    @field:Schema(
        description = "Template engine. SIMPLE replaces {{ identifier }} placeholders. JINJA2 renders a restricted Jinja2 template.",
        example = "SIMPLE",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var engine: PromptEngine? = null

    @field:Schema(
        description = "Template body. SIMPLE templates use {{ identifier }} placeholders.",
        example = "You are helping {{ user }}.",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 100000,
    )
    var content: String = ""

    @field:Schema(
        description = "Optional free-form description",
        example = "Default system prompt for chat",
        maxLength = 2000,
    )
    var description: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PromptTemplateUpdateRequest) return false
        return name == other.name &&
            engine == other.engine &&
            content == other.content &&
            description == other.description
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (engine?.hashCode() ?: 0)
        result = 31 * result + content.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }
}
