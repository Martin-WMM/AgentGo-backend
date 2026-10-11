package com.agentgo.prompts.exception

import org.springframework.http.HttpStatus

/** Raised when the caller is authenticated but is not allowed to use the template. */
class UnauthorizedPromptTemplateException : PromptTemplateException(
    code = 9005,
    responseType = "PROMPT-005",
    status = HttpStatus.FORBIDDEN,
    message = "You do not have access to this prompt template.",
)
