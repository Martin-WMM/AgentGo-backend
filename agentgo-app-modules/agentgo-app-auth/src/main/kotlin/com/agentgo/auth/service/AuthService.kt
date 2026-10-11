package com.agentgo.auth.service

import com.agentgo.dto.auth.SessionResponse
import com.agentgo.dto.auth.ProfileResponse
import com.agentgo.dto.auth.ProfileUpdateRequest

/** Provides access to the currently authenticated AgentGo user session. */
interface AuthService {
    /**
     * Returns the current session, including the authenticated user's OIDC claims when present.
     */
    fun currentSession(): SessionResponse

    /**
     * Returns the current profile. Identity fields come from Authentik and are mirrored locally.
     * Extended personal details come from the AgentGo profile row.
     *
     * @return the current user's merged profile
     * @throws IllegalStateException when there is no authenticated session or Authentik cannot be queried
     */
    fun currentProfile(): ProfileResponse

    /**
     * Updates identity fields in Authentik and stores extended personal details locally.
     *
     * @param request the editable profile fields
     * @return the profile stored after synchronization
     * @throws com.agentgo.auth.exception.InvalidProfileRequestException when a profile field is invalid
     * @throws IllegalStateException when there is no authenticated session or Authentik cannot be updated
     */
    fun updateProfile(request: ProfileUpdateRequest): ProfileResponse
}
