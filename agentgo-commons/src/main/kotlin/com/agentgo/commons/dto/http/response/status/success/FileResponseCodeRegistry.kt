package com.agentgo.commons.dto.http.response.status.success

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for successful file-operation response codes. */
object FileResponseCodeRegistry {
    const val UPLOAD_COMPLETED = 2201
    const val UPLOAD_COMPLETED_TYPE = "FILE-UPLOAD-COMPLETED"
    const val LIST_RETRIEVED = 2200
    const val LIST_RETRIEVED_TYPE = "FILE-LIST-RETRIEVED"
    val codeDescriptions = mapOf(
        UPLOAD_COMPLETED_TYPE to ResponseCodeDescription("A workspace file was uploaded successfully.", "FILE", "Workspace file uploaded."),
        LIST_RETRIEVED_TYPE to ResponseCodeDescription("Workspace files were retrieved successfully.", "FILE", "Workspace files listed."),
    )
    val responseTypesByCode = mapOf(UPLOAD_COMPLETED to UPLOAD_COMPLETED_TYPE, LIST_RETRIEVED to LIST_RETRIEVED_TYPE)
}
