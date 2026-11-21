package com.agentgo.commons.dto.http.response.status.success

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for successful global system-event response codes. */
object EventsResponseCodeRegistry {
    const val LIST_RETRIEVED = 9800
    const val LIST_RETRIEVED_TYPE = "EVENT-LIST-RETRIEVED"
    const val CREATED = 9801
    const val CREATED_TYPE = "EVENT-CREATED"
    const val RETRIEVED = 9802
    const val RETRIEVED_TYPE = "EVENT-RETRIEVED"
    const val UPDATED = 9803
    const val UPDATED_TYPE = "EVENT-UPDATED"
    const val DELETED = 9804
    const val DELETED_TYPE = "EVENT-DELETED"

    val codeDescriptions = mapOf(
        LIST_RETRIEVED_TYPE to ResponseCodeDescription(
            "Global system events were listed successfully.",
            "EVENT",
            "System events listed.",
        ),
        CREATED_TYPE to ResponseCodeDescription(
            "A global system event was created successfully.",
            "EVENT",
            "System event created.",
        ),
        RETRIEVED_TYPE to ResponseCodeDescription(
            "A global system event was retrieved successfully.",
            "EVENT",
            "System event retrieved.",
        ),
        UPDATED_TYPE to ResponseCodeDescription(
            "A global system event was updated successfully.",
            "EVENT",
            "System event updated.",
        ),
        DELETED_TYPE to ResponseCodeDescription(
            "A global system event was deleted successfully.",
            "EVENT",
            "System event deleted.",
        ),
    )

    val responseTypesByCode = mapOf(
        LIST_RETRIEVED to LIST_RETRIEVED_TYPE,
        CREATED to CREATED_TYPE,
        RETRIEVED to RETRIEVED_TYPE,
        UPDATED to UPDATED_TYPE,
        DELETED to DELETED_TYPE,
    )
}
