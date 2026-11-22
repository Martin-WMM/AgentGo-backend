package com.agentgo.core.event

/**
 * Family of a streamed agent event.
 *
 * @property eventName SSE event name shared by every event in the family
 */
enum class AgentEventKind(val eventName: String) {
    AGENT_LIFECYCLE("agent.lifecycle"),
    NODE_LIFECYCLE("node.lifecycle"),
    MESSAGE("message"),
    TOOL_LIFECYCLE("tool.lifecycle"),
    CLI_LIFECYCLE("cli.lifecycle"),
}
