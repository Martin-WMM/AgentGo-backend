package com.agentgo.a2a.exception

import org.springframework.http.HttpStatus

/** Raised when groupName, name, and creator already identify an A2A agent. */
class DuplicateA2aAgentNameException : A2aException(
    code = 9933,
    responseType = "A2A-003",
    status = HttpStatus.CONFLICT,
    message = "An A2A agent with this group and name already exists.",
)
