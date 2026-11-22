package com.agentgo.ai.controller

import com.agentgo.ai.controller.apidoc.AgentStreamControllerApiDoc
import com.agentgo.ai.exception.AgentStreamBusyException
import com.agentgo.ai.properties.AgentStreamProperties
import com.agentgo.ai.service.AgentStreamRuntimeEnricher
import com.agentgo.ai.service.AgentStreamService
import com.agentgo.commons.annotation.AgentGoLogApi
import com.agentgo.dto.agent.AgentStreamRequest
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.RejectedExecutionException
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * SSE entry for one AgentGo run.
 *
 * The controller submits the stream to the agent worker pool. The service turns
 * runtime events into common SSE payloads, and each payload is written as it arrives.
 * The request-thread authentication is copied into a fresh SecurityContext on the
 * worker so clearing the HTTP request context cannot wipe the long-lived stream.
 *
 * @property agentStreamService stream application service
 * @property agentStreamExecutor worker pool that runs one stream per task
 * @property properties SSE timeout
 */
@RestController
class AgentStreamController(
    private val agentStreamService: AgentStreamService,
    private val agentStreamExecutor: ExecutorService,
    private val properties: AgentStreamProperties,
) : AgentStreamControllerApiDoc {
    @AgentGoLogApi(name = "agent.stream")
    override fun stream(request: AgentStreamRequest): SseEmitter {
        val emitter = SseEmitter(properties.timeout.toMillis())
        val requestId = UUID.randomUUID().toString()
        val authentication = SecurityContextHolder.getContext().authentication
        val ownerUserId = resolveOwnerUserId(authentication)
        try {
            agentStreamExecutor.execute {
                val workerContext = SecurityContextHolder.createEmptyContext()
                workerContext.authentication = authentication
                SecurityContextHolder.setContext(workerContext)
                try {
                    write(
                        emitter,
                        AgentStreamRuntimeEnricher.enrich(request, ownerUserId),
                        requestId,
                    )
                } finally {
                    SecurityContextHolder.clearContext()
                }
            }
        } catch (ex: RejectedExecutionException) {
            throw AgentStreamBusyException()
        }
        return emitter
    }

    private fun write(emitter: SseEmitter, request: AgentStreamRequest, requestId: String) {
        try {
            agentStreamService.stream(request, requestId) { event ->
                emitter.send(
                    SseEmitter.event()
                        .id(event.eventId)
                        .name(event.eventName)
                        .data(event),
                )
            }
            emitter.complete()
        } catch (ex: Exception) {
            emitter.completeWithError(ex)
        }
    }

    private fun resolveOwnerUserId(authentication: Authentication?): String? {
        if (
            authentication == null ||
            !authentication.isAuthenticated ||
            authentication is AnonymousAuthenticationToken ||
            authentication.name.isNullOrBlank()
        ) {
            return null
        }
        return authentication.name
    }
}
