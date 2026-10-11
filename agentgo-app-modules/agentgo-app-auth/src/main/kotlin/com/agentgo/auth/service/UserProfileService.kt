package com.agentgo.auth.service

import com.agentgo.dto.auth.ProfileResponse
import com.agentgo.dto.auth.ProfileUpdateRequest

/**
 * Synchronizes the authenticated user's Authentik identity into the local profile
 * and stores AgentGo-owned personal details for later agent access.
 */
interface UserProfileService {
    /**
     * Loads the Authentik user, refreshes the local identity snapshot, and returns the merged profile.
     *
     * Extended fields already stored for [subject] are left unchanged.
     *
     * @param subject OIDC subject stored as the profile owner
     * @param fallbackEmail email used when Authentik cannot look the subject up by UUID
     * @return the merged profile
     * @throws IllegalStateException when Authentik does not return the user
     */
    fun currentProfile(subject: String, fallbackEmail: String?): ProfileResponse

    /**
     * Writes identity fields to Authentik, then stores the returned identity plus local details.
     *
     * @param subject OIDC subject stored as the profile owner
     * @param fallbackEmail email used when Authentik cannot look the subject up by UUID
     * @param request editable profile fields
     * @return the profile stored after synchronization
     * @throws com.agentgo.auth.exception.InvalidProfileRequestException when a field is invalid
     * @throws IllegalStateException when Authentik does not return the user
     */
    fun updateProfile(subject: String, fallbackEmail: String?, request: ProfileUpdateRequest): ProfileResponse
}
