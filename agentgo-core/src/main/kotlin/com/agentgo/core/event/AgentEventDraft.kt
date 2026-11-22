package com.agentgo.core.event

/**
 * Event payload a node asks the runtime to publish.
 *
 * Nodes fill only the fields they know. [com.agentgo.core.runtime.AgentRuntime]
 * adds the thread, headers, and metadata that belong to the run.
 *
 * @property kind event family
 * @property phase phase or message role name
 * @property nodeId graph node that published the event, when the event belongs to a node
 * @property content optional text carried by the event
 * @property toolName tool name for tool and CLI events
 * @property finished whether this event ends the stream
 */
data class AgentEventDraft(
    val kind: AgentEventKind,
    val phase: String,
    val nodeId: String? = null,
    val content: String? = null,
    val toolName: String? = null,
    val finished: Boolean = false,
) {
    companion object {
        /** Agent-level lifecycle event. */
        fun agent(
            phase: AgentLifecyclePhase,
            content: String? = null,
            finished: Boolean = false,
        ): AgentEventDraft = AgentEventDraft(
            kind = AgentEventKind.AGENT_LIFECYCLE,
            phase = phase.name,
            content = content,
            finished = finished,
        )

        /** Node lifecycle event. */
        fun node(
            nodeId: String,
            phase: NodeLifecyclePhase,
            content: String? = null,
        ): AgentEventDraft = AgentEventDraft(
            kind = AgentEventKind.NODE_LIFECYCLE,
            phase = phase.name,
            nodeId = nodeId,
            content = content,
        )

        /** User, assistant, or tool-result message. */
        fun message(
            nodeId: String,
            role: AgentMessageRole,
            content: String,
        ): AgentEventDraft = AgentEventDraft(
            kind = AgentEventKind.MESSAGE,
            phase = role.name,
            nodeId = nodeId,
            content = content,
        )

        /** Tool execution lifecycle event. */
        fun tool(
            nodeId: String,
            toolName: String,
            phase: ToolLifecyclePhase,
            content: String? = null,
        ): AgentEventDraft = AgentEventDraft(
            kind = AgentEventKind.TOOL_LIFECYCLE,
            phase = phase.name,
            nodeId = nodeId,
            toolName = toolName,
            content = content,
        )

        /** CLI execution lifecycle event. */
        fun cli(
            nodeId: String,
            toolName: String,
            phase: CliLifecyclePhase,
            content: String? = null,
        ): AgentEventDraft = AgentEventDraft(
            kind = AgentEventKind.CLI_LIFECYCLE,
            phase = phase.name,
            nodeId = nodeId,
            toolName = toolName,
            content = content,
        )
    }
}
