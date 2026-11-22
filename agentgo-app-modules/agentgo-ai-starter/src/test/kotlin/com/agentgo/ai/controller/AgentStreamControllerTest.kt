package com.agentgo.ai.controller

import com.agentgo.ai.exception.AgentStreamBusyException
import com.agentgo.ai.properties.AgentStreamProperties
import com.agentgo.ai.service.AgentStreamService
import com.agentgo.dto.agent.AgentStreamRequest
import java.util.concurrent.ExecutorService
import java.util.concurrent.RejectedExecutionException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AgentStreamControllerTest {
    @Test
    fun `stream submits the run to the agent executor`() {
        val service = mock<AgentStreamService>()
        val executor = mock<ExecutorService>()
        val controller = AgentStreamController(service, executor, AgentStreamProperties())

        val emitter = controller.stream(AgentStreamRequest())

        assertNotNull(emitter)
        verify(executor).execute(any())
    }

    @Test
    fun `stream rejects work when the executor is full`() {
        val executor = mock<ExecutorService>()
        whenever(executor.execute(any())).thenThrow(RejectedExecutionException("full"))
        val controller = AgentStreamController(mock(), executor, AgentStreamProperties())

        assertFailsWith<AgentStreamBusyException> {
            controller.stream(AgentStreamRequest())
        }
    }
}
