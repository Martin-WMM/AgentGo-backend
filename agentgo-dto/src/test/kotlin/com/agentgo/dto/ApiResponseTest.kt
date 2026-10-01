package com.agentgo.dto

import kotlin.test.Test
import kotlin.test.assertEquals

class ApiResponseTest {
    @Test
    fun wrapsResponseData() {
        assertEquals("ok", ApiResponse("ok").data)
    }
}
