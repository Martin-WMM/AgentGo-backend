package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for resource-operation failure codes. */
object ResourceResponseCodeRegistry {
    val codeDescriptions = mapOf(
        "RESOURCE-001" to ResponseCodeDescription("Requested resource does not exist.", "RESOURCE", "The requested resource was not found."),
        "RESOURCE-002" to ResponseCodeDescription("Resource is in a state that prevents the operation.", "RESOURCE", "The resource is not available for this operation."),
        "RESOURCE-003" to ResponseCodeDescription("Resource update conflicts with its current version or state.", "RESOURCE", "The resource was changed and cannot be updated."),
        "RESOURCE-004" to ResponseCodeDescription("Requested resource operation is unsupported.", "RESOURCE", "This operation is not supported for the resource."),
    )
    val responseTypesByCode = (4001..4004).associateWith { "RESOURCE-%03d".format(it - 4000) }
}
