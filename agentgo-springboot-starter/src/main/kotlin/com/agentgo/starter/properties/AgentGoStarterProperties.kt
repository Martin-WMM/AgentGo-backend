package com.agentgo.starter.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("agentgo.starter")
data class AgentGoStarterProperties(
    val enabled: Boolean = true,
)
