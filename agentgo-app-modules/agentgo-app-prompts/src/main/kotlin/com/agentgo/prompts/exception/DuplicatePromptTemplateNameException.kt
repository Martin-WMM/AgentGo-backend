package com.agentgo.prompts.exception

import org.springframework.http.HttpStatus

/** Raised when the authenticated user already owns a template with the same name. */
class DuplicatePromptTemplateNameException : PromptTemplateException(
    code = 9003,
    responseType = "PROMPT-003",
    status = HttpStatus.CONFLICT,
    message = "A prompt template with this name already exists for this user.",
)
