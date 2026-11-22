package com.agentgo.ai.exception

import com.agentgo.commons.dto.http.response.status.error.AgentResponseCodeRegistry
import org.springframework.http.HttpStatus

/**
 * Raised when every agent stream worker is busy.
 *
 * @param message safe client-facing failure message
 */
class AgentStreamBusyException(
    message: String = "Agent stream workers are busy. Retry later.",
) : AgentStreamException(
    code = AgentResponseCodeRegistry.BUSY,
    responseType = AgentResponseCodeRegistry.BUSY_TYPE,
    status = HttpStatus.SERVICE_UNAVAILABLE,
    message = message,
)
