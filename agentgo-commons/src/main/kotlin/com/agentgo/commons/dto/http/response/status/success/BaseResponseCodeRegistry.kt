package com.agentgo.commons.dto.http.response.status.success

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for base successful response codes. */
object BaseResponseCodeRegistry {
    const val SUCCESS = 0
    const val SUCCESS_TYPE = "SUCCESS"
    val codeDescriptions = mapOf(
        SUCCESS_TYPE to ResponseCodeDescription("The operation completed successfully.", "SUCCESS", "Request completed successfully."),
    )
    val responseTypesByCode = mapOf(SUCCESS to SUCCESS_TYPE)
}
