package com.agentgo.file.exception

import org.springframework.http.HttpStatus

/** Raised when deleting a workspace that still contains files. */
class WorkspaceNotEmptyException : WorkspaceException(
    code = 2304,
    responseType = "WORKSPACE-004",
    status = HttpStatus.CONFLICT,
    message = "Delete all files in the workspace before removing it.",
)
