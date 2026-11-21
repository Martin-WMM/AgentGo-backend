package com.agentgo.file.exception

import org.springframework.http.HttpStatus

/** Raised when a workspace id is unknown for the authenticated owner. */
class WorkspaceNotFoundException : WorkspaceException(
    code = 2301,
    responseType = "WORKSPACE-001",
    status = HttpStatus.NOT_FOUND,
    message = "The requested workspace was not found.",
)
