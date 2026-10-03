package com.agentgo.commons.dto.http.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * One-based page of response data.
 *
 * @param T type of items in [data]
 * @property data items in the current page
 * @property pageSize page size requested by the caller
 * @property totalElements total number of matching items
 * @property totalPages total number of available pages
 * @property empty whether the current page has no items
 * @property first whether this is the first page
 * @property last whether this is the last page
 * @property size actual number of items in this page
 * @property number one-based current page number
 */
@Schema(description = "One-based page of response data")
data class PageResponse<T>(
    @field:Schema(description = "Items in the current page", requiredMode = Schema.RequiredMode.REQUIRED)
    val data: List<T>,
    @field:Schema(description = "Page size requested by the caller", example = "20", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    val pageSize: Int,
    @field:Schema(description = "Total number of matching items", example = "42", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    val totalElements: Long,
    @field:Schema(description = "Total number of available pages", example = "3", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    val totalPages: Int,
    @field:Schema(description = "Whether the current page has no items", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    val empty: Boolean,
    @field:Schema(description = "Whether this is the first page", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    val first: Boolean,
    @field:Schema(description = "Whether this is the last page", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    val last: Boolean,
    @field:Schema(description = "Actual number of items in this page", example = "20", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    val size: Int,
    @field:Schema(description = "One-based current page number", example = "1", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    val number: Int,
)
