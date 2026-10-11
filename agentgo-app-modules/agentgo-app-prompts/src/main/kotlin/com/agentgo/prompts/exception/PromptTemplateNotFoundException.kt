package com.agentgo.prompts.exception

import org.springframework.http.HttpStatus

/** Raised when a prompt template id is unknown for the authenticated owner. */
class PromptTemplateNotFoundException : PromptTemplateException(
    code = 9001,
    responseType = "PROMPT-001",
    status = HttpStatus.NOT_FOUND,
    message = "The requested prompt template was not found.",
)
