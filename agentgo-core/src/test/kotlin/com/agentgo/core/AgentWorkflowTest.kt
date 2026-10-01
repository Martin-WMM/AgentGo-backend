package com.agentgo.core

import kotlin.test.Test
import kotlin.test.assertEquals

class AgentWorkflowTest {
    @Test
    fun executesInput() {
        assertEquals("hello", DefaultAgentWorkflow().execute(" hello "))
    }
}
