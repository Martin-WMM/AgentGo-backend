package com.agentgo.file.exception

import org.springframework.http.HttpStatus

open class FileException(
    val code: Int,
    val responseType: String,
    val status: HttpStatus,
    override val message: String,
) : RuntimeException(message)
