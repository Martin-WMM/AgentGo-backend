package com.agentgo.dto.auth

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Editable fields for the authenticated user's profile.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 *
 * Display name, email, and avatar URL are written to Authentik. The remaining fields
 * are stored by AgentGo for later agent access.
 */
@Schema(
    description = "Editable fields for the authenticated user's profile. Display name, email, " +
        "and avatar URL are written to Authentik. The remaining fields are stored by AgentGo.",
)
class ProfileUpdateRequest {
    @field:Schema(
        description = "User's display name",
        example = "Ada Lovelace",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 1,
        maxLength = 150,
    )
    var displayName: String = ""

    @field:Schema(description = "User's email address", example = "ada@example.com", format = "email", maxLength = 254)
    var email: String? = null

    @field:Schema(
        description = "Optional avatar URL. Absolute http(s) URLs and AgentGo avatar paths are accepted.",
        example = "https://example.com/avatar.png",
        format = "uri",
        maxLength = 2048,
    )
    var avatarUrl: String? = null

    @field:Schema(description = "Optional phone number. Send null or blank to clear it.", example = "+1 202 555 0100", maxLength = 32)
    var phoneNumber: String? = null

    @field:Schema(
        description = "Optional personal description. Send null or blank to clear it.",
        example = "Mathematician working on analytical engines.",
        maxLength = 2000,
    )
    var description: String? = null

    @field:Schema(description = "Optional occupation or job title. Send null or blank to clear it.", example = "Mathematician", maxLength = 128)
    var occupation: String? = null

    @field:Schema(description = "Optional organization or employer. Send null or blank to clear it.", example = "Analytical Engines", maxLength = 255)
    var organization: String? = null

    @field:Schema(
        description = "Optional homepage URL. Send null or blank to clear it.",
        example = "https://example.com/ada",
        format = "uri",
        maxLength = 2048,
    )
    var homepageUrl: String? = null

    @field:Schema(description = "Optional free-form location. Send null or blank to clear it.", example = "London", maxLength = 255)
    var location: String? = null

    @field:Schema(description = "Optional IANA time zone id. Send null or blank to clear it.", example = "Europe/London", maxLength = 64)
    var timezone: String? = null

    @field:Schema(description = "Optional preferred BCP 47 language tag. Send null or blank to clear it.", example = "en", maxLength = 35)
    var preferredLanguage: String? = null
}
