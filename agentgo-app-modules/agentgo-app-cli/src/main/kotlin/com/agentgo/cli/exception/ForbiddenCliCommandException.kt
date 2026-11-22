package com.agentgo.cli.exception

import org.springframework.http.HttpStatus

/**
 * Raised when a CLI script targets a denied command or uses blocked shell syntax.
 *
 * @param message safe client-facing failure message
 */
class ForbiddenCliCommandException(
    message: String = "This CLI command is not allowed.",
) : CliException(
    code = 9304,
    responseType = "CLI-004",
    status = HttpStatus.FORBIDDEN,
    message = message,
)
