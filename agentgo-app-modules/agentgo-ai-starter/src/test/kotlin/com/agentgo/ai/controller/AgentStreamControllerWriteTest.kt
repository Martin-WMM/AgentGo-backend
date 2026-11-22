package com.agentgo.ai.controller

import com.agentgo.ai.properties.AgentStreamProperties
import com.agentgo.ai.service.AgentStreamService
import com.agentgo.commons.dto.http.response.CommonEventResponse
import com.agentgo.dto.agent.AgentStreamEvent
import com.agentgo.dto.agent.AgentStreamRequest
import java.util.concurrent.ExecutorService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

class AgentStreamControllerWriteTest {
    @Test
    fun `stream worker copies security context and owner metadata`() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("ada", "n/a", AuthorityUtils.NO_AUTHORITIES)
        val service = mock<AgentStreamService>()
        whenever(
            service.stream(
                any<AgentStreamRequest>(),
                any<String>(),
                any<(CommonEventResponse<AgentStreamEvent>) -> Unit>(),
            ),
        ).thenAnswer { invocation ->
            val emit = invocation.getArgument<(CommonEventResponse<AgentStreamEvent>) -> Unit>(2)
            emit(
                CommonEventResponse(
                    eventName = "agent.lifecycle",
                    namespace = "agentgo.agent",
                    data = AgentStreamEvent(
                        kind = "agent.lifecycle",
                        phase = "COMPLETED",
                        threadId = "t",
                        nodeId = null,
                        content = "ok",
                        toolName = null,
                        headers = emptyMap(),
                        metadata = emptyMap(),
                    ),
                    finished = true,
                    requestId = invocation.getArgument(1),
                    eventType = "AGENT_LIFECYCLE",
                ),
            )
            null
        }
        val executor = mock<ExecutorService>()
        doAnswer { invocation ->
            invocation.getArgument<Runnable>(0).run()
            null
        }.whenever(executor).execute(any())
        val controller = AgentStreamController(service, executor, AgentStreamProperties())

        assertNotNull(controller.stream(AgentStreamRequest()))
        val requestCaptor = argumentCaptor<AgentStreamRequest>()
        verify(service).stream(
            requestCaptor.capture(),
            any<String>(),
            any<(CommonEventResponse<AgentStreamEvent>) -> Unit>(),
        )
        assertEquals("ada", requestCaptor.firstValue.metadata["ownerUserId"])
        // The HTTP request context must not be the shared worker context object.
        assertNull(SecurityContextHolder.getContext().authentication)
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `stream worker completes with error when the service fails`() {
        val service = mock<AgentStreamService>()
        whenever(
            service.stream(
                any<AgentStreamRequest>(),
                any<String>(),
                any<(CommonEventResponse<AgentStreamEvent>) -> Unit>(),
            ),
        ).thenThrow(IllegalStateException("boom"))
        val executor = mock<ExecutorService>()
        doAnswer { invocation ->
            invocation.getArgument<Runnable>(0).run()
            null
        }.whenever(executor).execute(any())
        val controller = AgentStreamController(service, executor, AgentStreamProperties())

        assertNotNull(controller.stream(AgentStreamRequest()))
    }
}
