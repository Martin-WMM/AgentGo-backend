package com.agentgo.ai.service.impl

import com.agentgo.ai.checkpoint.CheckpointDataSource
import com.agentgo.ai.exception.InvalidAgentStreamRequestException
import com.agentgo.ai.service.AgentStreamService
import com.agentgo.ai.service.ChatSessionTurnWriter
import com.agentgo.commons.dto.http.response.CommonEventResponse
import com.agentgo.core.event.AgentEventKind
import com.agentgo.core.runtime.AgentNodeIds
import com.agentgo.core.runtime.AgentRuntime
import com.agentgo.core.runtime.AgentStreamCommand
import com.agentgo.dto.agent.AgentStreamEvent
import com.agentgo.dto.agent.AgentStreamRequest
import org.slf4j.LoggerFactory

/**
 * Adapts [AgentRuntime] events into [CommonEventResponse] SSE payloads.
 *
 * When [turnWriter] is present and `metadata.sessionId` is set, a successful run
 * appends the user prompt and assistant answer to chat history.
 *
 * @param runtime agent runtime that fills shared event fields
 * @param modelId model id written into the graph state
 * @param turnWriter optional chat-history persistence hook
 * @param checkpointDataSource optional leak-safe datasource released after each run
 */
class AgentStreamServiceImpl(
    private val runtime: AgentRuntime,
    private val modelId: String,
    private val turnWriter: ChatSessionTurnWriter? = null,
    private val checkpointDataSource: CheckpointDataSource? = null,
) : AgentStreamService {
    override fun stream(
        request: AgentStreamRequest,
        requestId: String,
        emit: (CommonEventResponse<AgentStreamEvent>) -> Unit,
    ) {
        validate(request)
        var plan: String? = null
        var answer: String? = null
        var completed = false
        try {
            runtime.stream(command(request), { event ->
                if (event.kind == AgentEventKind.MESSAGE && event.nodeId == AgentNodeIds.PLAN) {
                    plan = event.content
                }
                if (
                    event.kind == AgentEventKind.NODE_LIFECYCLE &&
                    event.nodeId == AgentNodeIds.OUTPUT &&
                    event.phase.equals("COMPLETED", ignoreCase = true)
                ) {
                    answer = event.content
                }
                if (event.finished && event.phase.equals("COMPLETED", ignoreCase = true)) {
                    completed = true
                }
                emit(
                    CommonEventResponse(
                        eventName = event.name,
                        namespace = NAMESPACE,
                        data = AgentStreamEvent(
                            kind = event.name,
                            phase = event.phase,
                            threadId = event.threadId,
                            nodeId = event.nodeId,
                            content = event.content,
                            toolName = event.toolName,
                            headers = event.headers,
                            metadata = event.metadata,
                        ),
                        finished = event.finished,
                        requestId = requestId,
                        eventType = event.kind.name,
                    ),
                )
            })
            persistTurnIfNeeded(request, plan, answer, completed)
        } finally {
            checkpointDataSource?.releaseAll()
        }
    }

    private fun persistTurnIfNeeded(
        request: AgentStreamRequest,
        plan: String?,
        answer: String?,
        completed: Boolean,
    ) {
        val writer = turnWriter ?: return
        if (!completed) {
            return
        }
        val sessionId = request.metadata[SESSION_ID_METADATA]?.toLongOrNull() ?: return
        val ownerUserId = request.metadata[OWNER_USER_ID_METADATA]?.takeIf { it.isNotBlank() } ?: return
        val userContent = request.state.input.trim()
        val assistantContent = answer?.trim().orEmpty()
        if (userContent.isEmpty() || assistantContent.isEmpty()) {
            return
        }
        try {
            writer.recordSuccessfulTurn(
                sessionId = sessionId,
                ownerUserId = ownerUserId,
                userContent = userContent,
                assistantContent = assistantContent,
                plan = plan?.trim()?.takeIf { it.isNotEmpty() },
            )
        } catch (ex: Exception) {
            log.warn(
                "Failed to persist chat turn sessionId={} ownerUserId={}",
                sessionId,
                ownerUserId,
                ex,
            )
        }
    }

    private fun command(request: AgentStreamRequest): AgentStreamCommand =
        AgentStreamCommand(
            input = request.state.input,
            modelId = modelId,
            threadId = request.config.threadId.trim(),
            headers = request.headers,
            metadata = request.metadata,
        )

    private fun validate(request: AgentStreamRequest) {
        if (request.headers.keys.any { it.isBlank() } || request.metadata.keys.any { it.isBlank() }) {
            throw InvalidAgentStreamRequestException()
        }
    }

    companion object {
        const val NAMESPACE: String = "agentgo.agent"
        const val SESSION_ID_METADATA: String = "sessionId"
        const val OWNER_USER_ID_METADATA: String = "ownerUserId"
        private val log = LoggerFactory.getLogger(AgentStreamServiceImpl::class.java)
    }
}
