package com.agentgo.auth.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "agentgo.auth")
data class AuthProperties(
    val uiBaseUrl: String = "http://localhost:5173",
    val registrationId: String = "authentik",
    val authentikApiBaseUrl: String = "http://localhost:9000",
    val authentikApiToken: String = "",
    val authentikBrowserBaseUrl: String = "http://localhost:9000",
    val authentikApplicationSlug: String = "agentgo",
)
