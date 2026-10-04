package com.agentgo.commons.dto.http.response.status.success

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for successful authentication-domain response codes. */
object AuthResponseCodeRegistry {
    const val AVATAR_UPDATED = 1200
    const val AVATAR_UPDATED_TYPE = "AUTH-AVATAR-UPDATED"
    val codeDescriptions = mapOf(
        AVATAR_UPDATED_TYPE to ResponseCodeDescription("The current user's avatar was updated successfully.", "AUTHENTICATION", "Avatar updated."),
    )
    val responseTypesByCode = mapOf(AVATAR_UPDATED to AVATAR_UPDATED_TYPE)
}
