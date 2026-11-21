package com.agentgo.commons.dto.http.response.status.success

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for successful file-workspace response codes. */
object WorkspaceResponseCodeRegistry {
    const val LIST_RETRIEVED = 3200
    const val LIST_RETRIEVED_TYPE = "WORKSPACE-LIST-RETRIEVED"
    const val CREATED = 3201
    const val CREATED_TYPE = "WORKSPACE-CREATED"
    const val RETRIEVED = 3202
    const val RETRIEVED_TYPE = "WORKSPACE-RETRIEVED"
    const val UPDATED = 3203
    const val UPDATED_TYPE = "WORKSPACE-UPDATED"
    const val DELETED = 3204
    const val DELETED_TYPE = "WORKSPACE-DELETED"

    val codeDescriptions = mapOf(
        LIST_RETRIEVED_TYPE to ResponseCodeDescription(
            "User-owned file workspaces were listed successfully.",
            "WORKSPACE",
            "Workspaces listed.",
        ),
        CREATED_TYPE to ResponseCodeDescription(
            "A user-owned file workspace was created successfully.",
            "WORKSPACE",
            "Workspace created.",
        ),
        RETRIEVED_TYPE to ResponseCodeDescription(
            "A user-owned file workspace was retrieved successfully.",
            "WORKSPACE",
            "Workspace retrieved.",
        ),
        UPDATED_TYPE to ResponseCodeDescription(
            "A user-owned file workspace was updated successfully.",
            "WORKSPACE",
            "Workspace updated.",
        ),
        DELETED_TYPE to ResponseCodeDescription(
            "A user-owned file workspace was deleted successfully.",
            "WORKSPACE",
            "Workspace deleted.",
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
