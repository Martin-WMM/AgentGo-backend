package com.agentgo.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GreetingServiceTest {
    private val service = GreetingService()

    @Test
    fun greetsNamedUser() {
        assertEquals("Hello, AgentGo!", service.greet("AgentGo"))
    }

    @Test
    fun rejectsBlankName() {
        assertFailsWith<IllegalArgumentException> { service.greet(" ") }
    }
}
