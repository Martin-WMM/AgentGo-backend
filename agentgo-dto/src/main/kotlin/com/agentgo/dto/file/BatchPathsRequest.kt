package com.agentgo.dto.file

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Request body for deleting several workspace files owned by the authenticated user.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 *
 * The batch is all-or-nothing: a missing or invalid path deletes nothing.
 */
@Schema(description = "Workspace file paths to delete in one all-or-nothing request")
class BatchPathsRequest {
    @field:Schema(
        description = "Relative workspace paths owned by the authenticated user. At most 100 paths.",
        example = "[\"1/notes.txt\", \"1/diagram.png\"]",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var paths: List<String>? = emptyList()
}
