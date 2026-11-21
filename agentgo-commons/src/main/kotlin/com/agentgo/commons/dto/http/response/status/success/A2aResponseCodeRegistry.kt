package com.agentgo.commons.dto.http.response.status.success

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for successful A2A agent management response codes. */
object A2aResponseCodeRegistry {
    const val LIST_RETRIEVED = 9900
    const val LIST_RETRIEVED_TYPE = "A2A-LIST-RETRIEVED"
    const val CREATED = 9901
    const val CREATED_TYPE = "A2A-CREATED"
    const val RETRIEVED = 9902
    const val RETRIEVED_TYPE = "A2A-RETRIEVED"
    const val UPDATED = 9903
    const val UPDATED_TYPE = "A2A-UPDATED"
    const val DELETED = 9904
    const val DELETED_TYPE = "A2A-DELETED"

    val codeDescriptions = mapOf(
        LIST_RETRIEVED_TYPE to ResponseCodeDescription(
            "User-owned A2A agents were listed successfully.",
            "A2A",
            "A2A agents listed.",
        ),
        CREATED_TYPE to ResponseCodeDescription(
            "A user-owned A2A agent was created successfully.",
            "A2A",
            "A2A agent created.",
        ),
        RETRIEVED_TYPE to ResponseCodeDescription(
            "A user-owned A2A agent was retrieved successfully.",
            "A2A",
            "A2A agent retrieved.",
        ),
        UPDATED_TYPE to ResponseCodeDescription(
            "A user-owned A2A agent was updated successfully.",
            "A2A",
            "A2A agent updated.",
        ),
        DELETED_TYPE to ResponseCodeDescription(
            "A user-owned A2A agent was deleted successfully.",
            "A2A",
            "A2A agent deleted.",
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
