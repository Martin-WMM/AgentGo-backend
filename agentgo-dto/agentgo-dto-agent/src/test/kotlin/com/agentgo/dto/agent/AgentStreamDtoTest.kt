package com.agentgo.dto.agent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AgentStreamDtoTest {
    @Test
    fun `request stores state config headers and metadata`() {
        val request = AgentStreamRequest().apply {
            state = AgentStreamState().apply { input = "hello" }
            config = AgentStreamConfig().apply { threadId = "thread-1" }
            headers = mapOf("Accept-Language" to "zh-CN")
            metadata = mapOf("source" to "ui")
        }

        assertEquals("hello", request.state.input)
        assertEquals("thread-1", request.config.threadId)
        assertEquals("zh-CN", request.headers["Accept-Language"])
        assertEquals("ui", request.metadata["source"])
    }

    @Test
    fun `request defaults nested values to empty`() {
        val request = AgentStreamRequest()

        assertEquals("", request.state.input)
        assertEquals("", request.config.threadId)
        assertTrue(request.headers.isEmpty())
        assertTrue(request.metadata.isEmpty())
    }

    @Test
    fun `event exposes every field`() {
        val event = sample()

        assertEquals("message", event.kind)
        assertEquals("USER", event.phase)
        assertEquals("thread-1", event.threadId)
        assertEquals("input", event.nodeId)
        assertEquals("hello", event.content)
        assertNull(event.toolName)
        assertEquals("zh-CN", event.headers["Accept-Language"])
        assertEquals("ui", event.metadata["source"])
    }

    @Test
    fun `event equality checks every field`() {
        val event = sample()
        val same = sample()

        assertEquals(event, same)
        assertEquals(event.hashCode(), same.hashCode())
    }

    private fun sample() = AgentStreamEvent(
        kind = "message",
        phase = "USER",
        threadId = "thread-1",
        nodeId = "input",
        content = "hello",
        toolName = null,
        headers = mapOf("Accept-Language" to "zh-CN"),
        metadata = mapOf("source" to "ui"),
    )
}
