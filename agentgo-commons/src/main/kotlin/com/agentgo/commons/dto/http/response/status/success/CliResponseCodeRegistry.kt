package com.agentgo.commons.dto.http.response.status.success

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for successful CLI execution response codes. */
object CliResponseCodeRegistry {
    const val EXECUTED = 9300
    const val EXECUTED_TYPE = "CLI-EXECUTED"

    val codeDescriptions = mapOf(
        EXECUTED_TYPE to ResponseCodeDescription(
            "A CLI script was executed and its captured output was returned.",
            "CLI",
            "CLI execution completed.",
        ),
    )

    val responseTypesByCode = mapOf(
        EXECUTED to EXECUTED_TYPE,
    )
}