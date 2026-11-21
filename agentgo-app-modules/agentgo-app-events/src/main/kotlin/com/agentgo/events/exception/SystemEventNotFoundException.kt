package com.agentgo.events.exception

import org.springframework.http.HttpStatus

/** Raised when a system event id does not exist. */
class SystemEventNotFoundException : SystemEventException(
    code = 9701,
    responseType = "EVENT-001",
    status = HttpStatus.NOT_FOUND,
    message = "The requested system event was not found.",
)
