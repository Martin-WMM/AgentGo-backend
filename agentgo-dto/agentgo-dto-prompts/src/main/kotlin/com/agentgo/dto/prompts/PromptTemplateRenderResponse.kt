package com.agentgo.dto.prompts

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Rendered text produced from a prompt template.
 *
 * @property output rendered template text
 */
@Schema(
    description = "Rendered text from POST /preview or POST /{id}/render. Success code is 9205 PROMPT-RENDERED.",
)
data class PromptTemplateRenderResponse(
    @field:Schema(
        description = "Rendered template text",
        example = "Hello Ada",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val output: String,
)
