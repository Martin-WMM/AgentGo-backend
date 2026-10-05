package com.agentgo.file.exception

import org.springframework.http.HttpStatus

/** Raised when the uploaded part contains no bytes. */
class EmptyFileException : FileException(
    code = 2012,
    responseType = "FILE-012",
    status = HttpStatus.BAD_REQUEST,
    message = "File must not be empty",
)
