package com.agentgo.events.exception

import org.springframework.http.HttpStatus

/**
 * Raised when a system-event request fails domain validation.
 *
 * @param message safe client-facing validation message
 */
class InvalidSystemEventRequestException(
    message: String = "The system event request is invalid.",
) : SystemEventException(
    code = 9702,
    responseType = "EVENT-002",
    status = HttpStatus.BAD_REQUEST,
    message = message,
)
