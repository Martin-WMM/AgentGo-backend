package com.agentgo.cli

import kotlin.test.Test
import kotlin.test.assertEquals

class AgentGoCommandsTest {
    @Test
    fun returnsVersion() {
        assertEquals("AgentGo CLI 0.1.0", AgentGoCommands().version())
    }
}
