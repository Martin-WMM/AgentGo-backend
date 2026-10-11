package com.agentgo.prompts.exception

import org.springframework.http.HttpStatus

/**
 * Raised when create, update, or preview payloads fail domain validation.
 *
 * @param message safe client-facing validation message
 */
class InvalidPromptTemplateRequestException(
    message: String = "The prompt template request is invalid.",
) : PromptTemplateException(
    code = 9002,
    responseType = "PROMPT-002",
    status = HttpStatus.BAD_REQUEST,
    message = message,
)
