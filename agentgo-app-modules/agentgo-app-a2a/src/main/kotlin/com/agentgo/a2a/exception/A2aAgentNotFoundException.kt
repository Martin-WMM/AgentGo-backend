package com.agentgo.a2a.exception

import org.springframework.http.HttpStatus

/** Raised when an A2A agent id is unknown for the authenticated owner. */
class A2aAgentNotFoundException : A2aException(
    code = 9931,
    responseType = "A2A-001",
    status = HttpStatus.NOT_FOUND,
    message = "The requested A2A agent was not found.",
)
