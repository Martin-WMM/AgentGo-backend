package com.agentgo.dto.settings

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Information density applied by AgentGo clients for the authenticated user.
 */
@Schema(
    description = "Information density applied across AgentGo clients",
    allowableValues = ["COMFORTABLE", "COMPACT"],
)
enum class UiDensity {
    /** Default spacing and type size. */
    COMFORTABLE,

    /** Tighter spacing and a smaller root type size. */
    COMPACT,
}
