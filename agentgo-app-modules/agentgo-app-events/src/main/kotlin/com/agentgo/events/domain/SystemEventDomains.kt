package com.agentgo.events.domain

/**
 * Canonical `domain` values for [com.agentgo.events.annotation.RecordSystemEvent].
 *
 * Prefer these constants over free-form strings so Event Logs filters stay
 * stable across modules. Add a new constant when a capability starts recording.
 */
object SystemEventDomains {
    /** User-owned AI model configuration. */
    const val MODELS = "models"

    /** User-owned prompt templates and revisions. */
    const val PROMPTS = "prompts"

    /** User-owned MCP Streamable HTTP servers. */
    const val MCP_SERVERS = "mcp-servers"

    /** User-owned A2A sub-agents. */
    const val A2A_AGENTS = "a2a-agents"

    /** Workspace metadata managed by the file module. */
    const val WORKSPACES = "workspaces"

    /** Per-user AgentGo client settings. */
    const val SETTINGS = "settings"

    /** Authenticated CLI / shell executions. */
    const val CLI = "cli"

    /** Authentication and session lifecycle. */
    const val AUTH = "auth"

    /** The system-event log itself (manual API writes only). */
    const val EVENTS = "events"
}
