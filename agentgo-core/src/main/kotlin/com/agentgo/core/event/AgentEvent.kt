package com.agentgo.core.event

/**
 * One event emitted by an agent run.
 *
 * Thread, header parameters, and metadata are copied from the run by
 * [com.agentgo.core.runtime.AgentRuntime].
 *
 * @property kind event family
 * @property name SSE event name
 * @property phase phase or message role
 * @property threadId conversation thread from the run config
 * @property nodeId graph node that published the event
 * @property content optional text
 * @property toolName tool name for tool and CLI events
 * @property headers header parameters accepted with the run
 * @property metadata caller metadata accepted with the run
 * @property finished whether this event ends the stream
 */
data class AgentEvent(
    val kind: AgentEventKind,
    val name: String,
    val phase: String,
    val threadId: String,
    val nodeId: String?,
    val content: String?,
    val toolName: String?,
    val headers: Map<String, String>,
    val metadata: Map<String, String>,
    val finished: Boolean,
)
