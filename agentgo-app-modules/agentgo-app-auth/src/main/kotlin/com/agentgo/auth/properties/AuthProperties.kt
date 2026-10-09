package com.agentgo.auth.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "agentgo.auth")
data class AuthProperties(
    val uiBaseUrl: String = "http://localhost:5173",
    val oidcClientId: String = "agentgo",
    val registrationId: String = "authentik",
    val authentikApiBaseUrl: String = "http://localhost:9000",
    val authentikApiToken: String = "",
    val authentikBrowserBaseUrl: String = "http://localhost:9000",
    val authentikApplicationSlug: String = "agentgo",
) {
    /**
     * Builds the UI URL used after Authentik completes RP-initiated logout.
     *
     * @return absolute UI URL with the signed-out gate query parameter
     */
    fun signedOutUiUrl(): String = "${uiBaseUrl.trimEnd('/')}/?signedOut=1"
}
