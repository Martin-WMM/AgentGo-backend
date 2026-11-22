package com.agentgo.cli.exception

import org.springframework.http.HttpStatus

/**
 * Raised when a CLI execution request fails domain validation beyond bean validation.
 *
 * @param message safe client-facing validation message
 */
class InvalidCliScriptException(
    message: String = "The CLI script is invalid.",
) : CliException(
    code = 9301,
    responseType = "CLI-001",
    status = HttpStatus.BAD_REQUEST,
    message = message,
)