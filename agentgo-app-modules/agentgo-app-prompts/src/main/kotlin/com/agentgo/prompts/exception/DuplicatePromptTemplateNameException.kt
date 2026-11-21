package com.agentgo.prompts.exception

import org.springframework.http.HttpStatus

/** Raised when groupName, name, and creator already identify a prompt template. */
class DuplicatePromptTemplateNameException : PromptTemplateException(
    code = 9003,
    responseType = "PROMPT-003",
    status = HttpStatus.CONFLICT,
    message = "A prompt template with this group, name, and creator already exists.",
)
