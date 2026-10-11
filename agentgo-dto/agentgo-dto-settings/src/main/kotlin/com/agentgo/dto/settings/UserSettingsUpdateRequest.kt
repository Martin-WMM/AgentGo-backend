package com.agentgo.dto.settings

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Replacement body for the authenticated user's system settings.
 *
 * Ownership is taken from the security context. The client must not send a user id.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 */
@Schema(description = "Request body for PUT /api/v1/settings. Replaces the authenticated user's system settings.")
class UserSettingsUpdateRequest {
    @field:Schema(
        description = "Color theme applied across AgentGo clients",
        example = "DARK",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var theme: UiTheme = UiTheme.SYSTEM

    @field:Schema(
        description = "Interface language applied across AgentGo clients",
        example = "EN",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var locale: UiLocale = UiLocale.EN

    @field:Schema(
        description = "Whether the workspace sidebar is collapsed",
        example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var sidebarCollapsed: Boolean = false

    @field:Schema(
        description = "Information density applied across AgentGo clients",
        example = "COMPACT",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var density: UiDensity = UiDensity.COMFORTABLE

    @field:Schema(
        description = "Whether animations and transitions are minimized",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var reduceMotion: Boolean = false
}
