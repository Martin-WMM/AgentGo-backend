package com.agentgo.dto.a2a

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Operator-facing availability of a managed A2A agent.
 */
@Schema(
    description = "Operator status of a user-owned A2A agent",
    allowableValues = ["UNKNOWN", "AVAILABLE", "UNAVAILABLE", "ERROR"],
)
enum class A2aAgentStatus {
    /** The agent has not been marked available or unavailable. */
    UNKNOWN,

    /** The agent is available for collaboration. */
    AVAILABLE,

    /** The agent is intentionally offline. */
    UNAVAILABLE,

    /** The last known agent state is an error. */
    ERROR,
}
