package com.agentgo.commons.dto.http.response.status

import com.agentgo.commons.dto.http.response.status.error.ErrorResponseCodeRegistry
import com.agentgo.commons.dto.http.response.status.success.FileResponseCodeRegistry
import com.agentgo.commons.dto.http.response.status.success.McpResponseCodeRegistry
import com.agentgo.commons.dto.http.response.status.success.ModelsResponseCodeRegistry
import com.agentgo.commons.dto.http.response.status.success.PromptsResponseCodeRegistry
import com.agentgo.commons.dto.http.response.status.success.SettingsResponseCodeRegistry
import com.agentgo.commons.dto.http.response.status.success.WorkspaceResponseCodeRegistry
import kotlin.test.Test
import kotlin.test.assertEquals

class ResponseCodeRegistryTest {
    @Test
    fun resolvesSplitErrorCodeDescriptions() {
        val description = ErrorResponseCodeRegistry.description("FILE-001")

        assertEquals("FILE", description?.category)
        assertEquals("File name or path is invalid.", description?.meaning)
        assertEquals("MODEL", ErrorResponseCodeRegistry.description("MODEL-001")?.category)
        assertEquals("WORKSPACE", ErrorResponseCodeRegistry.description("WORKSPACE-001")?.category)
        assertEquals("PROMPT", ErrorResponseCodeRegistry.description("PROMPT-001")?.category)
        assertEquals("MCP", ErrorResponseCodeRegistry.description("MCP-001")?.category)
        assertEquals("MCP", ErrorResponseCodeRegistry.description("MCP-006")?.category)
    }

    @Test
    fun exposesSpecificSuccessCodeDescriptions() {
        assertEquals(
            "FILE",
            FileResponseCodeRegistry.codeDescriptions[FileResponseCodeRegistry.UPLOAD_COMPLETED_TYPE]?.category,
        )
        assertEquals(
            "MODEL",
            ModelsResponseCodeRegistry.codeDescriptions[ModelsResponseCodeRegistry.CREATED_TYPE]?.category,
        )
        assertEquals(
            "WORKSPACE",
            WorkspaceResponseCodeRegistry.codeDescriptions[WorkspaceResponseCodeRegistry.CREATED_TYPE]?.category,
        )
        assertEquals(
            "PROMPT",
            PromptsResponseCodeRegistry.codeDescriptions[PromptsResponseCodeRegistry.CREATED_TYPE]?.category,
        )
        assertEquals(
            "SETTINGS",
            SettingsResponseCodeRegistry.codeDescriptions[SettingsResponseCodeRegistry.RETRIEVED_TYPE]?.category,
        )
        assertEquals(
            "MCP",
            McpResponseCodeRegistry.codeDescriptions[McpResponseCodeRegistry.CREATED_TYPE]?.category,
        )
    }

    @Test
    fun resolvesNumericCodeToItsResponseTypeAndDescription() {
        assertEquals("FILE-001", ResponseCodeRegistry.responseType(2001))
        assertEquals("File name or path is invalid.", ResponseCodeRegistry.description(2001)?.meaning)
        assertEquals("FILE-011", ResponseCodeRegistry.responseType(2011))
        assertEquals("Uploaded file exceeds the configured size limit.", ResponseCodeRegistry.description(2011)?.meaning)
        assertEquals("FILE-012", ResponseCodeRegistry.responseType(2012))
        assertEquals("FILE-013", ResponseCodeRegistry.responseType(2013))
        assertEquals("FILE-014", ResponseCodeRegistry.responseType(2014))
        assertEquals("WORKSPACE-001", ResponseCodeRegistry.responseType(2301))
        assertEquals("WORKSPACE-CREATED", ResponseCodeRegistry.responseType(3201))
        assertEquals("A user-owned file workspace was created successfully.", ResponseCodeRegistry.description(3201)?.meaning)
        assertEquals("MODEL-001", ResponseCodeRegistry.responseType(8001))
        assertEquals("MODEL-CREATED", ResponseCodeRegistry.responseType(8201))
        assertEquals("A user-owned model was created successfully.", ResponseCodeRegistry.description(8201)?.meaning)
        assertEquals("PROMPT-001", ResponseCodeRegistry.responseType(9001))
        assertEquals("PROMPT-RENDERED", ResponseCodeRegistry.responseType(9205))
        assertEquals("A prompt template was rendered successfully.", ResponseCodeRegistry.description(9205)?.meaning)
        assertEquals("SETTINGS-RETRIEVED", ResponseCodeRegistry.responseType(9400))
        assertEquals("SETTINGS-UPDATED", ResponseCodeRegistry.responseType(9401))
        assertEquals(
            "The authenticated user's system settings were saved successfully.",
            ResponseCodeRegistry.description(9401)?.meaning,
        )
        assertEquals("MCP-001", ResponseCodeRegistry.responseType(9501))
        assertEquals("MCP-006", ResponseCodeRegistry.responseType(9506))
        assertEquals("MCP-CREATED", ResponseCodeRegistry.responseType(9601))
        assertEquals("MCP-PROBED", ResponseCodeRegistry.responseType(9605))
        assertEquals(
            "A user-owned MCP server was created successfully.",
            ResponseCodeRegistry.description(9601)?.meaning,
        )
    }
}
