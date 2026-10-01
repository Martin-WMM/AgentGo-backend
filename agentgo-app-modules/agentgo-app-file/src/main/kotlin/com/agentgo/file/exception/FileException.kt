package com.agentgo.file.exception

import org.springframework.http.HttpStatus

class FileException(
    val status: HttpStatus,
    override val message: String,
) : RuntimeException(message)
