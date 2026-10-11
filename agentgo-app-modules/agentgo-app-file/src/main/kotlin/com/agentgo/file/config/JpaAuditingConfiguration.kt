package com.agentgo.file.config

import java.util.Optional
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Supplies the JPA auditing actor for `@CreatedBy` and `@LastModifiedBy`.
 *
 * `creator` and `updatedBy` are the authenticated principal name from
 * [SecurityContextHolder]. Anonymous and missing sessions are rejected so a
 * placeholder such as `system` is never stored.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "agentGoAuditorAware")
class JpaAuditingConfiguration {
    @Bean
    fun agentGoAuditorAware(): AuditorAware<String> = AuditorAware {
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
