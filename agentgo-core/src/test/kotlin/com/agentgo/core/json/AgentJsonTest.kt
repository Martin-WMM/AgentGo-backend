package com.agentgo.core.json

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AgentJsonTest {
    @Test
    fun `arguments round trip through json`() {
        val json = AgentJson.writeArguments(linkedMapOf("key" to "alpha"))

        assertEquals(mapOf("key" to "alpha"), AgentJson.readArguments(json))
    }

    @Test
    fun `nested values are kept as json text`() {
        val arguments = AgentJson.readArguments("""{"key":"alpha","nested":{"n":1}}""")

        assertEquals("alpha", arguments["key"])
        assertEquals("{\"n\":1}", arguments["nested"])
    }

    @Test
    fun `invalid argument json becomes an empty map`() {
        assertTrue(AgentJson.readArguments("").isEmpty())
        assertTrue(AgentJson.readArguments("[]").isEmpty())
        assertTrue(AgentJson.readArguments("{").isEmpty())
    }
}
