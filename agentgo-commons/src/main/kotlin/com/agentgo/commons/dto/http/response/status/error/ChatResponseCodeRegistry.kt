package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for chat-session failure codes. */
object ChatResponseCodeRegistry {
    const val NOT_FOUND: Int = 10101
    const val NOT_FOUND_TYPE: String = "CHAT-001"
    const val INVALID: Int = 10102
    const val INVALID_TYPE: String = "CHAT-002"

    val codeDescriptions: Map<String, ResponseCodeDescription> = mapOf(
        NOT_FOUND_TYPE to ResponseCodeDescription(
            "Requested chat session does not exist for the authenticated user.",
            "CHAT",
            "The requested chat session was not found.",
        ),
        INVALID_TYPE to ResponseCodeDescription(
            "Chat session request parameters are invalid.",
            "CHAT",
            "The chat session request is invalid.",
        ),
    )

    val responseTypesByCode: Map<Int, String> = mapOf(
        NOT_FOUND to NOT_FOUND_TYPE,
        INVALID to INVALID_TYPE,
    )
}
