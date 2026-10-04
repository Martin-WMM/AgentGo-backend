package com.agentgo.commons.dto.http.request

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Request parameters for a one-based paginated query.
 *
 * @property page one-based page number
 * @property size maximum number of items in a page
 * @property sort fields used to sort the result, in precedence order
 * @property order direction applied to every field in [sort]
 */
@Schema(description = "One-based paginated query parameters")
data class PageRequest(
    @field:Schema(description = "One-based page number", example = "1", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    val page: Int = 1,
    @field:Schema(description = "Maximum number of items in a page", example = "20", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    val size: Int = 20,
    @field:Schema(description = "Fields used to sort the result, in precedence order", example = "[\"createdAt\", \"name\"]", type = "array")
    val sort: List<String> = emptyList(),
    @field:Schema(description = "Direction applied to every sort field", allowableValues = ["ASC", "DESC"], example = "ASC", requiredMode = Schema.RequiredMode.REQUIRED)
    val order: SortOrder = SortOrder.ASC,
) {
    init {
        require(page >= 1) { "Page number must be at least 1." }
        require(size >= 1) { "Page size must be at least 1." }
    }
}
