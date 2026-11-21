package com.agentgo.commons.dto.http.request

/**
 * Shared rules for identifier lists sent to batch-delete endpoints.
 *
 * Callers own the domain exception. This type only rejects empty, oversized, and non-positive lists.
 */
object BatchIds {
    /** Maximum number of identifiers accepted by one batch-delete request. */
    const val MAX_SIZE: Int = 100

    /**
     * Returns distinct positive identifiers, preserving the first occurrence of each id.
     *
     * @param ids client-supplied identifiers
     * @return de-duplicated identifiers safe to load and delete
     * @throws IllegalArgumentException when the list is empty, larger than [MAX_SIZE], or contains a non-positive id
     */
    fun requireDistinct(ids: List<Long>?): List<Long> {
        if (ids.isNullOrEmpty()) {
            throw IllegalArgumentException("At least one id is required.")
        }
        if (ids.size > MAX_SIZE) {
            throw IllegalArgumentException("At most $MAX_SIZE ids can be deleted at once.")
        }
        if (ids.any { it <= 0L }) {
            throw IllegalArgumentException("Every id must be a positive number.")
        }
        return ids.distinct()
    }

    /**
     * Returns [requireDistinct] or throws the caller's domain exception.
     *
     * @param ids client-supplied identifiers
     * @param invalid maps a validation message onto the resource's bad-request exception
     * @return de-duplicated positive identifiers
     */
    fun requireDistinctOr(ids: List<Long>?, invalid: (String) -> RuntimeException): List<Long> =
        try {
            requireDistinct(ids)
        } catch (failure: IllegalArgumentException) {
            throw invalid(failure.message ?: "The request is invalid.")
        }
}
