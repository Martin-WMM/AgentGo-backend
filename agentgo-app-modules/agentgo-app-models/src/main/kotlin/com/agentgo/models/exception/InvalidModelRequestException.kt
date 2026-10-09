package com.agentgo.models.exception

import org.springframework.http.HttpStatus

/**
 * Raised when create or update payloads fail domain validation.
 *
 * @param message safe client-facing validation message
 */
class InvalidModelRequestException(
    message: String = "The model request is invalid.",
) : ModelException(
    code = 8002,
    responseType = "MODEL-002",
    status = HttpStatus.BAD_REQUEST,
    message = message,
)
