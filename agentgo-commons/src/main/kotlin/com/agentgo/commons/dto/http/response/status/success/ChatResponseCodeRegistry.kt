package com.agentgo.commons.dto.http.response.status.success

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for successful chat-session response codes. */
object ChatResponseCodeRegistry {
    const val LIST_RETRIEVED = 10200
    const val LIST_RETRIEVED_TYPE = "CHAT-LIST-RETRIEVED"
    const val CREATED = 10201
    const val CREATED_TYPE = "CHAT-CREATED"
    const val RETRIEVED = 10202
    const val RETRIEVED_TYPE = "CHAT-RETRIEVED"
    const val UPDATED = 10203
    const val UPDATED_TYPE = "CHAT-UPDATED"
    const val DELETED = 10204
    const val DELETED_TYPE = "CHAT-DELETED"
    const val MESSAGES_RETRIEVED = 10205
    const val MESSAGES_RETRIEVED_TYPE = "CHAT-MESSAGES-RETRIEVED"

    val codeDescriptions = mapOf(
        LIST_RETRIEVED_TYPE to ResponseCodeDescription(
            "Chat sessions were listed successfully.",
            "CHAT",
            "Chat sessions listed.",
        ),
        CREATED_TYPE to ResponseCodeDescription(
            "A chat session was created successfully.",
            "CHAT",
            "Chat session created.",
        ),
        RETRIEVED_TYPE to ResponseCodeDescription(
            "A chat session was retrieved successfully.",
            "CHAT",
            "Chat session retrieved.",
        ),
        UPDATED_TYPE to ResponseCodeDescription(
            "A chat session was updated successfully.",
            "CHAT",
            "Chat session updated.",
        ),
        DELETED_TYPE to ResponseCodeDescription(
            "A chat session was deleted successfully.",
            "CHAT",
            "Chat session deleted.",
        ),
        MESSAGES_RETRIEVED_TYPE to ResponseCodeDescription(
            "Chat messages were listed successfully.",
            "CHAT",
            "Chat messages listed.",
        ),
    )

    val responseTypesByCode = mapOf(
        LIST_RETRIEVED to LIST_RETRIEVED_TYPE,
        CREATED to CREATED_TYPE,
        RETRIEVED to RETRIEVED_TYPE,
        UPDATED to UPDATED_TYPE,
        DELETED to DELETED_TYPE,
        MESSAGES_RETRIEVED to MESSAGES_RETRIEVED_TYPE,
    )
}
