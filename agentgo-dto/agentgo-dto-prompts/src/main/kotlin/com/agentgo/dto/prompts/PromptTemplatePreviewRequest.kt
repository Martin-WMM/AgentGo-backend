package com.agentgo.dto.prompts

import com.agentgo.commons.validation.TrimmedNotBlank
import com.agentgo.commons.validation.TrimmedSize
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

/**
 * Unsaved template and variables used to preview rendering.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 *
 * @property engine rendering engine
 * @property content template body
 * @property variables JSON object of template variables
 */
@Schema(
    description = "Request body for POST /api/v1/promptTemplates/preview. Renders an unsaved " +
        "engine and content with variables and does not write a row.",
)
class PromptTemplatePreviewRequest {
    @field:NotNull
    @field:Schema(
        description = "Template engine",
        example = "SIMPLE",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var engine: PromptEngine? = null

    @field:TrimmedNotBlank
    @field:TrimmedSize(min = 1, max = 100000)
    @field:Schema(
        description = "Template body to preview",
        example = "Hello {{ name }}",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 100000,
    )
    var content: String = ""

    @field:Schema(
        description = "JSON object of template variables. Values must be JSON scalars, arrays, or objects.",
        example = "{\"name\":\"Ada\"}",
    )
    var variables: Map<String, Any?> = emptyMap()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PromptTemplatePreviewRequest) return false
        return engine == other.engine &&
            content == other.content &&
            variables == other.variables
    }

    override fun hashCode(): Int {
        var result = engine?.hashCode() ?: 0
        result = 31 * result + content.hashCode()
        result = 31 * result + variables.hashCode()
        return result
    }
}
