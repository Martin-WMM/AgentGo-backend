package com.agentgo.ai.controller.apidoc

import com.agentgo.commons.api.ApiPaths
import com.agentgo.dto.agent.AgentStreamRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * Springdoc contract for the agent stream at `/api/v1/agent/stream`.
 *
 * Session cookie authentication is required. CSRF checks are skipped for this API prefix
 * so the SPA can POST JSON without a separate CSRF token header.
 */
@Tag(
    name = "Agent",
    description = "Authenticated agent stream at /api/v1/agent/stream. " +
        "The body carries state, config.threadId, header parameters, and metadata. " +
        "Events use the common SSE payload. Invalid requests use AGENT-001 (400). " +
        "A busy stream pool uses AGENT-002 (503). Missing authentication uses AUTH-002.",
)
@RequestMapping(ApiPaths.V1_AGENT)
interface AgentStreamControllerApiDoc {
    @Operation(
        summary = "Stream an agent run",
        description = "Runs the AgentGo graph and streams agent, node, message, tool, and CLI " +
            "lifecycle events. config.threadId is required.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "SSE stream of agent events"),
            ApiResponse(responseCode = "400", description = "Invalid agent stream request"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "503", description = "Agent stream workers are busy"),
        ],
    )
    @PostMapping(path = ["/stream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(@Valid @RequestBody request: AgentStreamRequest): SseEmitter
}
