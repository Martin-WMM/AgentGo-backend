package com.agentgo.settings.mapper

import com.agentgo.dto.settings.UserSettingsResponse
import com.agentgo.dto.settings.UserSettingsUpdateRequest
import com.agentgo.settings.entity.UserSettingsEntity

/**
 * Maps between system-settings persistence entities and API DTOs.
 */
interface UserSettingsMapper {
    /**
     * Converts a persisted entity into an API response.
     *
     * @param entity stored settings entity
     * @return response DTO marked as persisted
     * @throws IllegalArgumentException when required generated fields are missing
     */
    fun toResponse(entity: UserSettingsEntity): UserSettingsResponse

    /**
     * Returns the defaults served before a user has saved settings.
     *
     * @return response DTO with [UserSettingsResponse.persisted] set to `false`
     */
    fun unpersistedDefaults(): UserSettingsResponse

    /**
     * Builds a new entity owned by [ownerUserId] from an update request.
     *
     * @param ownerUserId authenticated owner
     * @param request replacement payload
     * @return unsaved entity
     */
    fun newEntity(ownerUserId: String, request: UserSettingsUpdateRequest): UserSettingsEntity

    /**
     * Copies [request] onto an existing entity.
     *
     * @param entity stored settings entity
     * @param request replacement payload
     */
    fun applyUpdate(entity: UserSettingsEntity, request: UserSettingsUpdateRequest)
}
