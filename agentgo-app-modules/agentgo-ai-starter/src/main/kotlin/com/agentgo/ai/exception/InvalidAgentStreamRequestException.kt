package com.agentgo.ai.exception

import com.agentgo.commons.dto.http.response.status.error.AgentResponseCodeRegistry
import org.springframework.http.HttpStatus

/**
 * Raised when header parameters or metadata keys are blank.
 *
 * @param message safe client-facing failure message
 */
class InvalidAgentStreamRequestException(
    message: String = "The agent stream request is invalid.",
) : AgentStreamException(
    code = AgentResponseCodeRegistry.INVALID,
    responseType = AgentResponseCodeRegistry.INVALID_TYPE,
    status = HttpStatus.BAD_REQUEST,
    message = message,
)
