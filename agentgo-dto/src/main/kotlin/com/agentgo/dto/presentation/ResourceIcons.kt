package com.agentgo.dto.presentation

import java.net.URI
import java.net.URISyntaxException

/**
 * Canonicalizes [ResourceIcon] values for every resource that stores an icon.
 *
 * Iconify ids use `prefix:name`. URLs must be absolute `http` or `https` addresses.
 * Callers map [IllegalArgumentException] onto their own API error code.
 */
object ResourceIcons {
    /** Maximum length of an Iconify id. */
    const val ICONIFY_VALUE_MAX_LENGTH: Int = 100

    /** Maximum length of an icon image URL. */
    const val URL_VALUE_MAX_LENGTH: Int = 2048

    private val ICONIFY_ID = Regex("^[a-z0-9]+(?:-[a-z0-9]+)*:[a-z0-9]+(?:[-_:][a-z0-9]+)*$")

    /**
     * Returns a trimmed icon, or null when [icon] itself is null.
     *
     * @param icon client-supplied icon, or null when the resource has none
     * @return canonical icon
     * @throws IllegalArgumentException when the source or value is not usable
     */
    fun canonicalize(icon: ResourceIcon?): ResourceIcon? {
        if (icon == null) return null
        val source = icon.source ?: throw IllegalArgumentException("The icon source is required.")
        val value = icon.value.trim()
        if (value.isEmpty()) {
            throw IllegalArgumentException("The icon value must not be blank.")
        }
        val canonicalValue = when (source) {
            IconSource.ICONIFY -> canonicalizeIconify(value)
            IconSource.URL -> canonicalizeUrl(value)
        }
        return ResourceIcon().apply {
            this.source = source
            this.value = canonicalValue
        }
    }

    private fun canonicalizeIconify(value: String): String {
        if (value.length > ICONIFY_VALUE_MAX_LENGTH || !ICONIFY_ID.matches(value)) {
            throw IllegalArgumentException(
                "The icon value must be an Iconify id such as lucide:bot.",
            )
        }
        return value
    }

    private fun canonicalizeUrl(value: String): String {
        if (value.length > URL_VALUE_MAX_LENGTH) {
            throw IllegalArgumentException(
                "The icon URL must be at most $URL_VALUE_MAX_LENGTH characters.",
            )
        }
        val uri = try {
            URI(value)
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("The icon URL is invalid.")
        } catch (_: URISyntaxException) {
            throw IllegalArgumentException("The icon URL is invalid.")
        }
        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") {
            throw IllegalArgumentException("The icon URL must use http or https.")
        }
        if (uri.host.isNullOrBlank()) {
            throw IllegalArgumentException("The icon URL must include a host.")
        }
        return value
    }
}
