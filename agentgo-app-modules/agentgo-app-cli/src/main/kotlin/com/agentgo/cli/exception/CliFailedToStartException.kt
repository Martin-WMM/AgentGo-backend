package com.agentgo.cli.exception

import org.springframework.http.HttpStatus

/**
 * Raised when the operating system cannot start the configured shell process.
 *
 * @param message safe client-facing failure message
 * @param cause underlying IOException from [ProcessBuilder.start]
 */
class CliFailedToStartException(
    message: String = "The CLI process could not be started.",
    cause: Throwable? = null,
) : CliException(
    code = 9302,
    responseType = "CLI-002",
    status = HttpStatus.INTERNAL_SERVER_ERROR,
    message = message,
) {
    init {
        cause?.let { initCause(it) }
    }
}