package com.agentgo.commons.dto.http.response.status.success

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for successful prompt-template response codes. */
object PromptsResponseCodeRegistry {
    const val LIST_RETRIEVED = 9200
    const val LIST_RETRIEVED_TYPE = "PROMPT-LIST-RETRIEVED"
    const val CREATED = 9201
    const val CREATED_TYPE = "PROMPT-CREATED"
    const val RETRIEVED = 9202
    const val RETRIEVED_TYPE = "PROMPT-RETRIEVED"
    const val UPDATED = 9203
    const val UPDATED_TYPE = "PROMPT-UPDATED"
    const val DELETED = 9204
    const val DELETED_TYPE = "PROMPT-DELETED"
    const val RENDERED = 9205
    const val RENDERED_TYPE = "PROMPT-RENDERED"
    const val REVISIONS_LISTED = 9206
    const val REVISIONS_LISTED_TYPE = "PROMPT-REVISIONS-LISTED"
    const val REVISION_RETRIEVED = 9207
    const val REVISION_RETRIEVED_TYPE = "PROMPT-REVISION-RETRIEVED"

    val codeDescriptions = mapOf(
        LIST_RETRIEVED_TYPE to ResponseCodeDescription(
            "User-owned prompt templates were listed successfully.",
            "PROMPT",
            "Prompt templates listed.",
        ),
        CREATED_TYPE to ResponseCodeDescription(
            "A user-owned prompt template was created successfully.",
            "PROMPT",
            "Prompt template created.",
        ),
        RETRIEVED_TYPE to ResponseCodeDescription(
            "A user-owned prompt template was retrieved successfully.",
            "PROMPT",
            "Prompt template retrieved.",
        ),
        UPDATED_TYPE to ResponseCodeDescription(
            "A user-owned prompt template was updated successfully.",
            "PROMPT",
            "Prompt template updated.",
        ),
        DELETED_TYPE to ResponseCodeDescription(
            "A user-owned prompt template was deleted successfully.",
            "PROMPT",
            "Prompt template deleted.",
        ),
        RENDERED_TYPE to ResponseCodeDescription(
            "A prompt template was rendered successfully.",
            "PROMPT",
            "Prompt template rendered.",
        ),
        REVISIONS_LISTED_TYPE to ResponseCodeDescription(
            "Prompt template revisions were listed successfully.",
            "PROMPT",
            "Prompt template revisions listed.",
        ),
        REVISION_RETRIEVED_TYPE to ResponseCodeDescription(
            "A prompt template revision was retrieved successfully.",
            "PROMPT",
            "Prompt template revision retrieved.",
        ),
    )

    val responseTypesByCode = mapOf(
        LIST_RETRIEVED to LIST_RETRIEVED_TYPE,
        CREATED to CREATED_TYPE,
        RETRIEVED to RETRIEVED_TYPE,
        UPDATED to UPDATED_TYPE,
        DELETED to DELETED_TYPE,
        RENDERED to RENDERED_TYPE,
        REVISIONS_LISTED to REVISIONS_LISTED_TYPE,
        REVISION_RETRIEVED to REVISION_RETRIEVED_TYPE,
    )
}
