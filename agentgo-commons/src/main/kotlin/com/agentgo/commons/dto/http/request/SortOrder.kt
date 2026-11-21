package com.agentgo.commons.dto.http.request

import io.swagger.v3.oas.annotations.media.Schema

/** Direction applied to fields in a [PageRequest.sort] request. */
@Schema(description = "Sort direction", allowableValues = ["ASC", "DESC"])
enum class SortOrder {
    /** Ascending sort order. */
    ASC,

    /** Descending sort order. */
    DESC,
}
