package com.agentgo.file.exception

import org.springframework.http.HttpStatus

/** Raised when the uploaded bytes cannot be read from the request. */
class UnreadableUploadException : FileException(
    code = 2014,
    responseType = "FILE-014",
    status = HttpStatus.BAD_REQUEST,
    message = "Unable to read uploaded file",
)
