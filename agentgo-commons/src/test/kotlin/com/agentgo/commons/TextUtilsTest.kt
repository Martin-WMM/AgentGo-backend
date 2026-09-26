package com.agentgo.commons

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TextUtilsTest {
    @Test
    fun trimsRequiredText() {
        assertEquals("AgentGo", TextUtils.requireText(" AgentGo ", "name"))
    }

    @Test
    fun rejectsBlankText() {
        assertFailsWith<IllegalArgumentException> { TextUtils.requireText(" ", "name") }
    }

    @Test
    fun wrapsResponseData() {
        assertEquals("ok", ApiResponse("ok").data)
    }
}
