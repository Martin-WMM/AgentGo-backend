package com.agentgo.commons.dto.http.response.status.success

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for successful model-management response codes. */
object ModelsResponseCodeRegistry {
    const val LIST_RETRIEVED = 8200
    const val LIST_RETRIEVED_TYPE = "MODEL-LIST-RETRIEVED"
    const val CREATED = 8201
    const val CREATED_TYPE = "MODEL-CREATED"
    const val RETRIEVED = 8202
    const val RETRIEVED_TYPE = "MODEL-RETRIEVED"
    const val UPDATED = 8203
    const val UPDATED_TYPE = "MODEL-UPDATED"
    const val DELETED = 8204
    const val DELETED_TYPE = "MODEL-DELETED"

    val codeDescriptions = mapOf(
        LIST_RETRIEVED_TYPE to ResponseCodeDescription(
            "User-owned models were listed successfully.",
            "MODEL",
            "Models listed.",
        ),
        CREATED_TYPE to ResponseCodeDescription(
            "A user-owned model was created successfully.",
            "MODEL",
            "Model created.",
        ),
        RETRIEVED_TYPE to ResponseCodeDescription(
            "A user-owned model was retrieved successfully.",
            "MODEL",
            "Model retrieved.",
        ),
        UPDATED_TYPE to ResponseCodeDescription(
            "A user-owned model was updated successfully.",
            "MODEL",
            "Model updated.",
        ),
        DELETED_TYPE to ResponseCodeDescription(
            "A user-owned model was deleted successfully.",
            "MODEL",
            "Model deleted.",
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
