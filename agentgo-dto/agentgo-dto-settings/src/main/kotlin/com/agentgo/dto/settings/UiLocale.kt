package com.agentgo.dto.settings

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Interface language applied by AgentGo clients for the authenticated user.
 *
 * Wire values stay stable uppercase identifiers. Clients map [EN] to `en` and [ZH_CN] to `zh-CN`.
 */
@Schema(
    description = "Interface language applied across AgentGo clients",
    allowableValues = ["EN", "ZH_CN"],
)
enum class UiLocale {
    /** English. */
    EN,

    /** Simplified Chinese. */
    ZH_CN,
}
