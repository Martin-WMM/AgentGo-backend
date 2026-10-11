package com.agentgo.dto.prompts

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Variables supplied when rendering a stored prompt template.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 *
 * @property variables JSON object of template variables
 */
@Schema(
    description = "Request body for POST /api/v1/promptTemplates/{id}/render. Supplies variables " +
        "for a stored template. The stored body is not changed.",
)
class PromptTemplateRenderRequest {
    @field:Schema(
        description = "JSON object of template variables. Values must be JSON scalars, arrays, or objects.",
        example = "{\"user\":\"Ada\"}",
    )
    var variables: Map<String, Any?> = emptyMap()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PromptTemplateRenderRequest) return false
        return variables == other.variables
    }

    override fun hashCode(): Int = variables.hashCode()
}
