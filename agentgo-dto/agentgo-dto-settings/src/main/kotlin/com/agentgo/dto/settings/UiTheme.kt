package com.agentgo.dto.settings

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Color theme applied by AgentGo clients for the authenticated user.
 *
 * [SYSTEM] follows the operating-system color scheme.
 */
@Schema(
    description = "Color theme applied across AgentGo clients",
    allowableValues = ["LIGHT", "DARK", "SYSTEM"],
)
enum class UiTheme {
    /** Always use the light theme. */
    LIGHT,

    /** Always use the dark theme. */
    DARK,

    /** Follow the operating-system color scheme. */
    SYSTEM,
}
