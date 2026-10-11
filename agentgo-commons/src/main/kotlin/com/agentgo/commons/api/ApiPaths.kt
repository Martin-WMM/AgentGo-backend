package com.agentgo.commons.api

/**
 * Canonical HTTP path prefixes shared by controllers, security, and download URLs.
 *
 * Prefer these constants over string literals so path changes stay consistent.
 */
object ApiPaths {
    /** Unversioned authentication base path. */
    const val AUTH: String = "/api/auth"

    /** Versioned business API root for v1. */
    const val V1_BASE: String = "/api/v1"

    /** Model-management endpoints under v1. */
    const val V1_MODELS: String = "/api/v1/models"

    /** Prompt-template endpoints under v1. */
    const val V1_PROMPT_TEMPLATES: String = "/api/v1/promptTemplates"

    /** MCP server management endpoints under v1 (Streamable HTTP only). */
    const val V1_MCP_SERVERS: String = "/api/v1/mcpServers"

    /** Authenticated user system-settings singleton under v1. */
    const val V1_SETTINGS: String = "/api/v1/settings"

    /** File-workspace metadata endpoints under v1. */
    const val V1_WORKSPACES: String = "/api/v1/workspaces"

    /** File and avatar endpoints under v1. */
    const val V1_FILES: String = "/api/v1/files"

    /** Ant-style matcher for all v1 file routes (security and filters). */
    const val V1_FILES_ANT: String = "/api/v1/files/**"

    /** Ant-style matcher for all v1 model routes (security). */
    const val V1_MODELS_ANT: String = "/api/v1/models/**"

    /** Ant-style matcher for all v1 prompt-template routes (security). */
    const val V1_PROMPT_TEMPLATES_ANT: String = "/api/v1/promptTemplates/**"

    /** Ant-style matcher for all v1 MCP server routes (security). */
    const val V1_MCP_SERVERS_ANT: String = "/api/v1/mcpServers/**"

    /** Ant-style matcher for all v1 workspace routes (security). */
    const val V1_WORKSPACES_ANT: String = "/api/v1/workspaces/**"

    /** Servlet URL pattern for the upload-size filter. */
    const val V1_FILES_URL_PATTERN: String = "/api/v1/files/*"

    /** Download URL for the current user's avatar. */
    const val V1_FILES_AVATAR: String = "/api/v1/files/avatar"

    /** Download and list base for workspace files (query path appended by callers). */
    const val V1_FILES_WORKSPACE: String = "/api/v1/files/workspace"

    /**
     * Builds a v1 resource base path from a relative segment.
     *
     * @param resource resource name without leading slash (for example models)
     * @return path such as /api/v1/models
     */
    fun v1(resource: String): String {
        val trimmed = resource.trim().trimStart('/')
        require(trimmed.isNotEmpty()) { "resource must not be blank" }
        return "$V1_BASE/$trimmed"
    }
}
