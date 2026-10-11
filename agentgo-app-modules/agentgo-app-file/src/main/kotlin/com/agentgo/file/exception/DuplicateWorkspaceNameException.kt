package com.agentgo.file.exception

import org.springframework.http.HttpStatus

/** Raised when the authenticated user already owns a workspace with the same name. */
class DuplicateWorkspaceNameException : WorkspaceException(
    code = 2303,
    responseType = "WORKSPACE-003",
    status = HttpStatus.CONFLICT,
    message = "A workspace with this name already exists.",
)
