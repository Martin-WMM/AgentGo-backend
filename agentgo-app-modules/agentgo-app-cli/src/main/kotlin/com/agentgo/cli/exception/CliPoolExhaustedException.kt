package com.agentgo.cli.exception

import org.springframework.http.HttpStatus

/**
 * Raised when every CLI worker is busy and the acquire timeout elapses.
 *
 * @param message safe client-facing failure message
 */
class CliPoolExhaustedException(
    message: String = "All CLI workers are busy. Retry later.",
) : CliException(
    code = 9303,
    responseType = "CLI-003",
    status = HttpStatus.SERVICE_UNAVAILABLE,
    message = message,
)
