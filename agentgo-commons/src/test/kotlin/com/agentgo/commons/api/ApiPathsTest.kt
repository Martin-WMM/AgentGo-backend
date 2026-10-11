package com.agentgo.commons.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ApiPathsTest {
    @Test
    fun exposesCanonicalV1Prefixes() {
        assertEquals("v1", ApiVersions.V1)
        assertEquals("/api/v1", ApiPaths.V1_BASE)
        assertEquals("/api/v1/models", ApiPaths.V1_MODELS)
        assertEquals("/api/v1/promptTemplates", ApiPaths.V1_PROMPT_TEMPLATES)
        assertEquals("/api/v1/mcpServers", ApiPaths.V1_MCP_SERVERS)
        assertEquals("/api/v1/settings", ApiPaths.V1_SETTINGS)
        assertEquals("/api/v1/workspaces", ApiPaths.V1_WORKSPACES)
        assertEquals("/api/v1/files", ApiPaths.V1_FILES)
        assertEquals("/api/auth", ApiPaths.AUTH)
        assertEquals("/api/v1/files/**", ApiPaths.V1_FILES_ANT)
        assertEquals("/api/v1/models/**", ApiPaths.V1_MODELS_ANT)
        assertEquals("/api/v1/promptTemplates/**", ApiPaths.V1_PROMPT_TEMPLATES_ANT)
        assertEquals("/api/v1/mcpServers/**", ApiPaths.V1_MCP_SERVERS_ANT)
        assertEquals("/api/v1/workspaces/**", ApiPaths.V1_WORKSPACES_ANT)
        assertEquals("/api/v1/files/*", ApiPaths.V1_FILES_URL_PATTERN)
        assertEquals("/api/v1/files/avatar", ApiPaths.V1_FILES_AVATAR)
        assertEquals("/api/v1/files/workspace", ApiPaths.V1_FILES_WORKSPACE)
    }

    @Test
    fun buildsV1ResourcePaths() {
        assertEquals("/api/v1/models", ApiPaths.v1("models"))
        assertEquals("/api/v1/files", ApiPaths.v1("/files"))
    }

    @Test
    fun rejectsBlankV1Resource() {
        assertFailsWith<IllegalArgumentException> { ApiPaths.v1("  ") }
    }
}
