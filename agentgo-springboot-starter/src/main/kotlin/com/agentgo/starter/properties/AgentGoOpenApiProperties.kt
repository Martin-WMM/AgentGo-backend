package com.agentgo.starter.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration for the shared AgentGo OpenAPI document metadata.
 *
 * @author Martin M. W.
 * @version 0.1.0-SNAPSHOT
 */
@ConfigurationProperties("agentgo.starter.openapi")
data class AgentGoOpenApiProperties(
    val title: String = "AgentGo API",
    val description: String = "AgentGo backend service API",
    val version: String = "v1",
    val termsOfServiceUrl: String = "",
    val contactName: String = "Martin",
    val contactUrl: String = "https://github.com/Martin-WMM/AgentGo-backend",
    val contactEmail: String = "",
    val licenseName: String = "Apache License 2.0",
    val licenseUrl: String = "https://www.apache.org/licenses/LICENSE-2.0",
    val serverUrl: String = "",
    val serverDescription: String = "",
)
