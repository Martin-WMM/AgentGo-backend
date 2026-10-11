package com.agentgo.file.exception

import org.springframework.http.HttpStatus

/** Raised when workspace create/update parameters fail validation. */
class InvalidWorkspaceRequestException(
    message: String = "The workspace request is invalid.",
) : WorkspaceException(
    code = 2302,
    responseType = "WORKSPACE-002",
    status = HttpStatus.BAD_REQUEST,
    message = message,
)
