package com.agentgo.commons.dto.http.request

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Request body for deleting several resources owned by the authenticated user.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 *
 * The batch is all-or-nothing: a missing, foreign, or invalid id deletes nothing.
 */
@Schema(description = "Identifiers to delete in one all-or-nothing request")
class BatchIdsRequest {
    @field:Schema(
        description = "Resource identifiers owned by the authenticated user. At most 100 positive ids.",
        example = "[1, 2, 3]",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var ids: List<Long>? = emptyList()
}
