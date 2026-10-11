package com.agentgo.commons.dto.http.response.status

import com.agentgo.commons.dto.http.response.status.error.ErrorResponseCodeRegistry
import com.agentgo.commons.dto.http.response.status.success.AuthResponseCodeRegistry
import com.agentgo.commons.dto.http.response.status.success.BaseResponseCodeRegistry
import com.agentgo.commons.dto.http.response.status.success.FileResponseCodeRegistry
import com.agentgo.commons.dto.http.response.status.success.McpResponseCodeRegistry
import com.agentgo.commons.dto.http.response.status.success.ModelsResponseCodeRegistry
import com.agentgo.commons.dto.http.response.status.success.PromptsResponseCodeRegistry
import com.agentgo.commons.dto.http.response.status.success.SettingsResponseCodeRegistry
import com.agentgo.commons.dto.http.response.status.success.WorkspaceResponseCodeRegistry

/**
 * Aggregate numeric response-code lookup table.
 *
 * Domain registries own code definitions; this registry only provides the canonical
 * `Int code` to `responseType` correspondence for response construction and lookup.
 */
object ResponseCodeRegistry {
    /** Canonical business-code table keyed by the numeric code exposed in responses. */
    val responseTypesByCode: Map<Int, String> = buildMap {
        putAll(BaseResponseCodeRegistry.responseTypesByCode)
        putAll(AuthResponseCodeRegistry.responseTypesByCode)
        putAll(FileResponseCodeRegistry.responseTypesByCode)
        putAll(WorkspaceResponseCodeRegistry.responseTypesByCode)
        putAll(ModelsResponseCodeRegistry.responseTypesByCode)
        putAll(PromptsResponseCodeRegistry.responseTypesByCode)
        putAll(McpResponseCodeRegistry.responseTypesByCode)
        putAll(SettingsResponseCodeRegistry.responseTypesByCode)
        putAll(ErrorResponseCodeRegistry.responseTypesByCode)
    }

    /** Returns the stable string response type for a numeric business code, or `null` when unknown. */
    fun responseType(code: Int): String? = responseTypesByCode[code]

    /** Returns documentation for a numeric business code, or `null` when unknown. */
    fun description(code: Int): ResponseCodeDescription? =
        responseType(code)?.let { type ->
            BaseResponseCodeRegistry.codeDescriptions[type]
                ?: AuthResponseCodeRegistry.codeDescriptions[type]
                ?: FileResponseCodeRegistry.codeDescriptions[type]
                ?: WorkspaceResponseCodeRegistry.codeDescriptions[type]
                ?: ModelsResponseCodeRegistry.codeDescriptions[type]
                ?: PromptsResponseCodeRegistry.codeDescriptions[type]
                ?: McpResponseCodeRegistry.codeDescriptions[type]
                ?: SettingsResponseCodeRegistry.codeDescriptions[type]
                ?: ErrorResponseCodeRegistry.description(type)
        }
}
