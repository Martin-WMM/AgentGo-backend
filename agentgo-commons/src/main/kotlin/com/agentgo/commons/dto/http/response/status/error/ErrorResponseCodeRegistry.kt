package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Aggregated lookup of all registered failure response codes. */
object ErrorResponseCodeRegistry {
    val codeDescriptions: Map<String, ResponseCodeDescription> = buildMap {
        putAll(AuthResponseCodeRegistry.codeDescriptions)
        putAll(FileResponseCodeRegistry.codeDescriptions)
        putAll(ModelsResponseCodeRegistry.codeDescriptions)
        putAll(ValidationResponseCodeRegistry.codeDescriptions)
        putAll(ResourceResponseCodeRegistry.codeDescriptions)
        putAll(IntegrationResponseCodeRegistry.codeDescriptions)
        putAll(DatabaseResponseCodeRegistry.codeDescriptions)
        putAll(SystemResponseCodeRegistry.codeDescriptions)
    }
    val responseTypesByCode: Map<Int, String> = buildMap {
        putAll(AuthResponseCodeRegistry.responseTypesByCode)
        putAll(FileResponseCodeRegistry.responseTypesByCode)
        putAll(ModelsResponseCodeRegistry.responseTypesByCode)
        putAll(ValidationResponseCodeRegistry.responseTypesByCode)
        putAll(ResourceResponseCodeRegistry.responseTypesByCode)
        putAll(IntegrationResponseCodeRegistry.responseTypesByCode)
        putAll(DatabaseResponseCodeRegistry.responseTypesByCode)
        putAll(SystemResponseCodeRegistry.responseTypesByCode)
    }

    fun description(code: String): ResponseCodeDescription? = codeDescriptions[code]
}
