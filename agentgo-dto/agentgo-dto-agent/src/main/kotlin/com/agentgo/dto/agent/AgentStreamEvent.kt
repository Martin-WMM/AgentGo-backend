package com.agentgo.dto.agent

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Payload of one agent SSE event.
 *
 * @property kind event family
 * @property phase lifecycle phase or message role
 * @property threadId conversation thread from the request config
 * @property nodeId graph node that published the event
 * @property content optional text
 * @property toolName tool name for tool and CLI events
 * @property headers header parameters accepted with the run
 * @property metadata caller metadata accepted with the run
 */
@Schema(description = "One agent stream event")
data class AgentStreamEvent(
    @field:Schema(description = "Event family", example = "node.lifecycle", requiredMode = Schema.RequiredMode.REQUIRED)
    val kind: String,
    @field:Schema(description = "Lifecycle phase or message role", example = "STARTED", requiredMode = Schema.RequiredMode.REQUIRED)
    val phase: String,
    @field:Schema(description = "Conversation thread id", example = "thread-7f3a", requiredMode = Schema.RequiredMode.REQUIRED)
    val threadId: String,
    @field:Schema(description = "Graph node that published the event", example = "llm")
    val nodeId: String?,
    @field:Schema(description = "Optional event text")
    val content: String?,
    @field:Schema(description = "Tool name for tool and CLI events")
    val toolName: String?,
    @field:Schema(description = "Header parameters accepted with the run", requiredMode = Schema.RequiredMode.REQUIRED)
    val headers: Map<String, String>,
    @field:Schema(description = "Caller metadata accepted with the run", requiredMode = Schema.RequiredMode.REQUIRED)
    val metadata: Map<String, String>,
)
