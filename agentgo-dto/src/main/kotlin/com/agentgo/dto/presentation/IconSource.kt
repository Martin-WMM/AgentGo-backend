package com.agentgo.dto.presentation

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Where a reusable resource icon is loaded from.
 *
 * `ICONIFY` stores an Iconify identifier such as `lucide:bot`. `URL` stores an
 * absolute http or https image address. Other resources should reuse this enum
 * instead of inventing a second icon field.
 */
@Schema(description = "Icon source. ICONIFY is an Iconify id. URL is an absolute http(s) image.")
enum class IconSource {
    ICONIFY,
    URL,
}
