package com.agentgo.settings.service

import com.agentgo.dto.settings.UserSettingsResponse
import com.agentgo.dto.settings.UserSettingsUpdateRequest

/**
 * Application service for the authenticated user's system settings.
 *
 * Implementations resolve the owner from the security context and never accept a
 * user identifier from the client. Reading settings does not create a row; the first
 * successful update inserts one.
 */
interface UserSettingsService {
    /**
     * Returns the current user's settings, or defaults when none have been saved.
     *
     * @return persisted settings, or unpersisted defaults
     * @throws com.agentgo.settings.exception.SettingsException when the caller is not authenticated
     */
    fun get(): UserSettingsResponse

    /**
     * Creates or replaces the current user's settings.
     *
     * @param request replacement payload
     * @return saved settings
     * @throws com.agentgo.settings.exception.SettingsException when the caller is not authenticated
     */
    fun update(request: UserSettingsUpdateRequest): UserSettingsResponse
}
