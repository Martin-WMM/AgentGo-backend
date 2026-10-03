package com.agentgo.starter.config

import com.agentgo.starter.AgentGoLogApiAspect
import com.agentgo.starter.properties.AgentGoOpenApiProperties
import com.agentgo.starter.properties.AgentGoStarterProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import java.time.Clock

/**
 * Provides reusable AgentGo foundation beans and API metadata configuration.
 *
 * @author Martin M. W.
 * @version 0.1.0-SNAPSHOT
 */
@AutoConfiguration(before = [JacksonAutoConfiguration::class])
@EnableConfigurationProperties(AgentGoStarterProperties::class, AgentGoOpenApiProperties::class)
@Import(AgentGoLogApiAspect::class)
class AgentGoStarterAutoConfiguration {
    @Bean
    fun agentGoClock(): Clock = Clock.systemUTC()

    @Bean
    @ConditionalOnMissingBean(ObjectMapper::class)
    fun agentGoObjectMapper(): ObjectMapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    @Bean
    @ConditionalOnMissingBean
    fun agentGoOpenAPI(
        properties: AgentGoOpenApiProperties = AgentGoOpenApiProperties(),
    ): OpenAPI {
        val info = Info()
            .title(properties.title)
            .description(properties.description)
            .version(properties.version)
            .contact(
                Contact()
                    .name(properties.contactName)
                    .url(properties.contactUrl)
                    .email(properties.contactEmail),
            )
            .license(
                License()
                    .name(properties.licenseName)
                    .url(properties.licenseUrl),
            )

        if (properties.termsOfServiceUrl.isNotBlank()) {
            info.termsOfService(properties.termsOfServiceUrl)
        }

        val openApi = OpenAPI()
            .components(Components())
            .info(info)

        if (properties.serverUrl.isNotBlank()) {
            openApi.servers(
                listOf(
                    Server()
                        .url(properties.serverUrl)
                        .description(properties.serverDescription.ifBlank { properties.serverUrl }),
                ),
            )
        }

        return openApi
    }
}
