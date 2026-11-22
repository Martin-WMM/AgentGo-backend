package com.agentgo.ai.service

import com.agentgo.commons.dto.http.response.CommonEventResponse
import com.agentgo.dto.agent.AgentStreamEvent
import com.agentgo.dto.agent.AgentStreamRequest

/**
 * Streams one agent run as common SSE payloads.
 *
 * The service validates the request, then forwards it to the agent runtime.
 */
interface AgentStreamService {
    /**
     * Emits every runtime event for [request].
     *
     * @param request state, config, header parameters, and metadata
     * @param requestId identifier copied onto every SSE payload
     * @param emit consumer invoked once per event on the caller thread
     */
    fun stream(
        request: AgentStreamRequest,
        requestId: String,
        emit: (CommonEventResponse<AgentStreamEvent>) -> Unit,
    )
}
