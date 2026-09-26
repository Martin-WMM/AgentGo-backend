package com.agentgo.starter

import java.time.Clock
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(AgentGoStarterProperties::class)
class AgentGoStarterAutoConfiguration {
    @Bean
    fun agentGoClock(): Clock = Clock.systemUTC()
}
