package com.agentgo.dto.settings

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * System settings that configure AgentGo clients for one authenticated user.
 *
 * When [persisted] is `false`, the values are defaults and have not been stored yet.
 * Clients should keep the current device preferences and save them with a later update.
 *
 * @property theme color theme
 * @property locale interface language
 * @property sidebarCollapsed whether the workspace sidebar starts collapsed
 * @property density information density
 * @property reduceMotion whether animations and transitions are minimized
 * @property persisted whether a stored row exists for the user
 * @property updatedAt last modification timestamp, or `null` when the settings are defaults
 */
@Schema(description = "Authenticated user's system settings returned by /api/v1/settings")
data class UserSettingsResponse(
    @field:Schema(
        description = "Color theme applied across AgentGo clients",
        example = "SYSTEM",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val theme: UiTheme,
    @field:Schema(
        description = "Interface language applied across AgentGo clients",
        example = "ZH_CN",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val locale: UiLocale,
    @field:Schema(
        description = "Whether the workspace sidebar is collapsed",
        example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val sidebarCollapsed: Boolean,
    @field:Schema(
        description = "Information density applied across AgentGo clients",
        example = "COMFORTABLE",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val density: UiDensity,
    @field:Schema(
        description = "Whether animations and transitions are minimized",
        example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val reduceMotion: Boolean,
    @field:Schema(
        description = "False when these values are defaults and no settings row exists yet",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val persisted: Boolean,
    @field:Schema(
        description = "Last modification timestamp in UTC, omitted from meaning when persisted is false",
        format = "date-time",
        example = "2026-10-11T10:15:30Z",
    )
    val updatedAt: Instant? = null,
)
