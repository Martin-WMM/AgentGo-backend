package com.agentgo.settings.mapper

import com.agentgo.dto.settings.UiDensity
import com.agentgo.dto.settings.UiLocale
import com.agentgo.dto.settings.UiTheme
import com.agentgo.dto.settings.UserSettingsResponse
import com.agentgo.dto.settings.UserSettingsUpdateRequest
import com.agentgo.settings.entity.UserSettingsEntity
import org.springframework.stereotype.Component

/**
 * Default [UserSettingsMapper] implementation used by the settings service layer.
 */
@Component
class UserSettingsMapperImpl : UserSettingsMapper {
    override fun toResponse(entity: UserSettingsEntity): UserSettingsResponse =
        UserSettingsResponse(
            theme = entity.theme,
            locale = entity.locale,
            sidebarCollapsed = entity.sidebarCollapsed,
            density = entity.density,
            reduceMotion = entity.reduceMotion,
            persisted = true,
            updatedAt = requireNotNull(entity.updatedAt) { "Persisted settings must have an update timestamp." },
        )

    override fun unpersistedDefaults(): UserSettingsResponse =
        UserSettingsResponse(
            theme = UiTheme.SYSTEM,
            locale = UiLocale.EN,
            sidebarCollapsed = false,
            density = UiDensity.COMFORTABLE,
            reduceMotion = false,
            persisted = false,
            updatedAt = null,
        )

    override fun newEntity(ownerUserId: String, request: UserSettingsUpdateRequest): UserSettingsEntity =
        UserSettingsEntity(
            ownerUserId = ownerUserId,
            theme = request.theme,
            locale = request.locale,
            sidebarCollapsed = request.sidebarCollapsed,
            density = request.density,
            reduceMotion = request.reduceMotion,
        )

    override fun applyUpdate(entity: UserSettingsEntity, request: UserSettingsUpdateRequest) {
        entity.theme = request.theme
        entity.locale = request.locale
        entity.sidebarCollapsed = request.sidebarCollapsed
        entity.density = request.density
        entity.reduceMotion = request.reduceMotion
    }
}
