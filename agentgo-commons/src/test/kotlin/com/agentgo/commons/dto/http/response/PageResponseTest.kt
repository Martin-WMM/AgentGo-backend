package com.agentgo.commons.dto.http.response

import kotlin.test.Test
import kotlin.test.assertEquals

class PageResponseTest {
    @Test
    fun exposesOneBasedPageMetadata() {
        val response = PageResponse(
            data = listOf("first", "second"), pageSize = 20, totalElements = 42, totalPages = 3,
            empty = false, first = true, last = false, size = 2, number = 1,
        )

        assertEquals(1, response.number)
        assertEquals(2, response.size)
    }
}
