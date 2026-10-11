package com.agentgo.commons.dto.http.response.status.success

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for successful user system-settings response codes. */
object SettingsResponseCodeRegistry {
    const val RETRIEVED = 9400
    const val RETRIEVED_TYPE = "SETTINGS-RETRIEVED"
    const val UPDATED = 9401
    const val UPDATED_TYPE = "SETTINGS-UPDATED"

    val codeDescriptions = mapOf(
        RETRIEVED_TYPE to ResponseCodeDescription(
            "The authenticated user's system settings were retrieved successfully.",
            "SETTINGS",
            "System settings retrieved.",
        ),
        UPDATED_TYPE to ResponseCodeDescription(
            "The authenticated user's system settings were saved successfully.",
            "SETTINGS",
            "System settings updated.",
        ),
    )

    val responseTypesByCode = mapOf(
        RETRIEVED to RETRIEVED_TYPE,
        UPDATED to UPDATED_TYPE,
    )
}
