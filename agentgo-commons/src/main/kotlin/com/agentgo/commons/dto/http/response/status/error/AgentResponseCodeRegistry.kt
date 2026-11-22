package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for agent stream failure codes. */
object AgentResponseCodeRegistry {
    const val INVALID: Int = 10001
    const val INVALID_TYPE: String = "AGENT-001"
    const val BUSY: Int = 10002
    const val BUSY_TYPE: String = "AGENT-002"

    val codeDescriptions: Map<String, ResponseCodeDescription> = mapOf(
        INVALID_TYPE to ResponseCodeDescription(
            "Agent stream request is invalid.",
            "AGENT",
            "The agent stream request is invalid.",
        ),
        BUSY_TYPE to ResponseCodeDescription(
            "Agent stream workers are busy.",
            "AGENT",
            "Agent stream workers are busy. Retry later.",
        ),
    )

    val responseTypesByCode: Map<Int, String> = mapOf(
        INVALID to INVALID_TYPE,
        BUSY to BUSY_TYPE,
    )
}
