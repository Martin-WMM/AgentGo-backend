package com.agentgo.a2a.exception

import org.springframework.http.HttpStatus

/**
 * Raised when create or update payloads fail domain validation.
 *
 * @param message safe client-facing validation message
 */
class InvalidA2aAgentRequestException(
    message: String = "The A2A agent request is invalid.",
) : A2aException(
    code = 9932,
    responseType = "A2A-002",
    status = HttpStatus.BAD_REQUEST,
    message = message,
)
