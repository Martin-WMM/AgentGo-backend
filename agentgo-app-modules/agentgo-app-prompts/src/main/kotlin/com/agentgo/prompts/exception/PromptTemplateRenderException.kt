package com.agentgo.prompts.exception

import org.springframework.http.HttpStatus

/**
 * Raised when a structurally valid template cannot be rendered.
 *
 * The client message stays generic so template-engine diagnostics are not returned.
 */
class PromptTemplateRenderException : PromptTemplateException(
    code = 9004,
    responseType = "PROMPT-004",
    status = HttpStatus.BAD_REQUEST,
    message = "The prompt template could not be rendered.",
)
