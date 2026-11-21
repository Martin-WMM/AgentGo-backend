package com.agentgo.events.support

import java.util.Optional
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Enables JPA auditing for system-event repository slice tests.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "systemEventTestAuditorAware")
class SystemEventRepositoryAuditingConfiguration {
    @Bean
    fun systemEventTestAuditorAware(): AuditorAware<String> = AuditorAware {
        val authentication = SecurityContextHolder.getContext().authentication
        if (
            authentication == null ||
            !authentication.isAuthenticated ||
            authentication is AnonymousAuthenticationToken ||
            authentication.name.isNullOrBlank()
        ) {
            throw IllegalStateException("Audit fields require an authenticated user.")
        }
        Optional.of(authentication.name)
    }
}
