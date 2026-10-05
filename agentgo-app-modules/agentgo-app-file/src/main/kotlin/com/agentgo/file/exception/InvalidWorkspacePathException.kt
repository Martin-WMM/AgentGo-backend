package com.agentgo.file.exception

import org.springframework.http.HttpStatus

/** Raised when a workspace path is blank or contains path traversal segments. */
class InvalidWorkspacePathException : FileException(
    code = 2013,
    responseType = "FILE-013",
    status = HttpStatus.BAD_REQUEST,
    message = "Workspace path is invalid",
)
