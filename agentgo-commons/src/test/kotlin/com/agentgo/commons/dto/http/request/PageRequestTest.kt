package com.agentgo.commons.dto.http.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PageRequestTest {
    @Test
    fun createsOneBasedRequestWithSorting() {
        val request = PageRequest(page = 1, size = 20, sort = listOf("createdAt", "name"), order = SortOrder.DESC)

        assertEquals(1, request.page)
        assertEquals(listOf("createdAt", "name"), request.sort)
        assertEquals(SortOrder.DESC, request.order)
    }

    @Test
    fun rejectsZeroBasedPagesAndNonPositivePageSizes() {
        assertFailsWith<IllegalArgumentException> { PageRequest(page = 0) }
        assertFailsWith<IllegalArgumentException> { PageRequest(size = 0) }
    }
}
