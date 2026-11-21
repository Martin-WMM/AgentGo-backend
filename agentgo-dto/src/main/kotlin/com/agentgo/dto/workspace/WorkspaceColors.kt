package com.agentgo.dto.workspace

/**
 * Accent colors clients may store on a file workspace.
 *
 * Values are dark enough for a white glyph and far enough apart in hue that
 * neighboring workspaces stay easy to tell apart. [DEFAULT] is used when a
 * caller omits a color.
 */
object WorkspaceColors {
    const val DEFAULT: String = "#1D4ED8"

    val palette: Set<String> = linkedSetOf(
        "#1D4ED8",
        "#0E7490",
        "#0F766E",
        "#15803D",
        "#B45309",
        "#C2410C",
        "#B91C1C",
        "#BE185D",
        "#6D28D9",
        "#4338CA",
    )

    /**
     * Returns the canonical palette value for [color], ignoring case and surrounding space.
     *
     * @param color client-supplied accent
     * @return matching palette entry, or null when [color] is not supported
     */
    fun canonicalize(color: String): String? =
        palette.firstOrNull { it.equals(color.trim(), ignoreCase = true) }
}
