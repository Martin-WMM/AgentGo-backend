package com.agentgo.commons.dto.http.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BatchIdsTest {
    @Test
    fun `requireDistinct keeps the first occurrence of each positive id`() {
        assertEquals(listOf(2L, 1L), BatchIds.requireDistinct(listOf(2L, 1L, 2L)))
    }

    @Test
    fun `requireDistinct rejects an empty oversized or non positive list`() {
        assertFailsWith<IllegalArgumentException> { BatchIds.requireDistinct(emptyList()) }
        assertFailsWith<IllegalArgumentException> { BatchIds.requireDistinct(null) }
        assertFailsWith<IllegalArgumentException> { BatchIds.requireDistinct(List(101) { it + 1L }) }
        assertFailsWith<IllegalArgumentException> { BatchIds.requireDistinct(listOf(1L, 0L)) }
    }

    @Test
    fun `requireDistinctOr translates validation failures`() {
        val failure = assertFailsWith<IllegalStateException> {
            BatchIds.requireDistinctOr(emptyList()) { IllegalStateException(it) }
        }

        assertEquals("At least one id is required.", failure.message)
    }
}
