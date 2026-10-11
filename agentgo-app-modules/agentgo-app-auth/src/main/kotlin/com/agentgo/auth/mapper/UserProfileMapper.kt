package com.agentgo.auth.mapper

import com.agentgo.auth.client.AuthentikUserResponse
import com.agentgo.auth.entity.UserProfileEntity
import com.agentgo.dto.auth.ProfileResponse
import com.agentgo.dto.auth.ProfileUpdateRequest

/** Maps between the local user profile, Authentik identity, and the profile API. */
interface UserProfileMapper {
    /**
     * Converts a persisted profile into the API response.
     *
     * @param entity persisted profile
     * @return profile returned to the authenticated user
     */
    fun toResponse(entity: UserProfileEntity): ProfileResponse

    /**
     * Copies Authentik identity fields onto [entity] without changing AgentGo-owned details.
     *
     * @param entity local profile to update
     * @param user Authentik user that is the identity source
     */
    fun applyIdentity(entity: UserProfileEntity, user: AuthentikUserResponse)

    /**
     * Copies AgentGo-owned profile details from an already normalized request.
     *
     * @param entity local profile to update
     * @param request normalized profile fields
     */
    fun applyDetails(entity: UserProfileEntity, request: ProfileUpdateRequest)
}
