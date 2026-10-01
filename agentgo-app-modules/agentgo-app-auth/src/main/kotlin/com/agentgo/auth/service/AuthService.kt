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
     * Returns the editable profile synchronized with the current Authentik user.
     *
     * @return the current user's Authentik-managed profile
     * @throws IllegalStateException when there is no authenticated session or Authentik cannot be queried
     */
    fun currentProfile(): ProfileResponse

    /**
     * Updates the current user's profile in Authentik and returns the saved profile.
     *
     * @param request the editable profile fields
     * @return the profile returned by Authentik after the update
     * @throws IllegalArgumentException when a required profile field is invalid
     * @throws IllegalStateException when there is no authenticated session or Authentik cannot be updated
     */
    fun updateProfile(request: ProfileUpdateRequest): ProfileResponse
}
