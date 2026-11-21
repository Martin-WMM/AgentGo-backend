package com.agentgo.auth.mapper

import com.agentgo.auth.client.AuthentikUserResponse
import com.agentgo.auth.entity.UserProfileEntity
import com.agentgo.dto.auth.ProfileResponse
import com.agentgo.dto.auth.ProfileUpdateRequest
import org.springframework.stereotype.Component

/** Default [UserProfileMapper] used by profile synchronization. */
@Component
class UserProfileMapperImpl : UserProfileMapper {
    override fun toResponse(entity: UserProfileEntity): ProfileResponse = ProfileResponse(
        username = entity.username,
        displayName = entity.displayName,
        email = entity.email,
        avatarUrl = entity.avatarUrl,
        phoneNumber = entity.phoneNumber,
        description = entity.description,
        occupation = entity.occupation,
        organization = entity.organization,
        homepageUrl = entity.homepageUrl,
        location = entity.location,
        timezone = entity.timezone,
        preferredLanguage = entity.preferredLanguage,
    )

    override fun applyIdentity(entity: UserProfileEntity, user: AuthentikUserResponse) {
        val username = user.username.trim()
        require(username.isNotEmpty()) { "Authentik username must not be blank" }
        entity.username = username.take(USERNAME_MAX)
        entity.displayName = user.name.trim().ifEmpty { username }.take(DISPLAY_NAME_MAX)
        entity.email = user.email?.trim()?.ifEmpty { null }
        // Keep an AgentGo-hosted avatar when Authentik only has a generated data: image.
        storageSafeAvatar(user.avatar)?.let { entity.avatarUrl = it }
        entity.authentikUserUuid = user.uuid
    }

    override fun applyDetails(entity: UserProfileEntity, request: ProfileUpdateRequest) {
        val requestedAvatar = request.avatarUrl?.trim()?.ifEmpty { null }
        when {
            requestedAvatar == null -> entity.avatarUrl = null
            requestedAvatar.startsWith("data:", ignoreCase = true) -> Unit
            else -> entity.avatarUrl = storageSafeAvatar(requestedAvatar)
        }
        entity.phoneNumber = request.phoneNumber
        entity.description = request.description
        entity.occupation = request.occupation
        entity.organization = request.organization
        entity.homepageUrl = request.homepageUrl
        entity.location = request.location
        entity.timezone = request.timezone
        entity.preferredLanguage = request.preferredLanguage
    }

    /**
     * Persists only compact avatar references. Authentik often returns large `data:image/`
     * SVGs that exceed `tbl_user_profile.avatar_url` and are unsuitable for agent context.
     */
    private fun storageSafeAvatar(avatar: String?): String? {
        val value = avatar?.trim()?.ifEmpty { null } ?: return null
        if (value.startsWith("data:", ignoreCase = true)) {
            return null
        }
        return value.takeIf { it.length <= AVATAR_URL_MAX }
    }

    private companion object {
        const val USERNAME_MAX = 150
        const val DISPLAY_NAME_MAX = 150
        const val AVATAR_URL_MAX = 2048
    }
}
