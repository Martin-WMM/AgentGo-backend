package com.agentgo.file.config

import java.util.Optional
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

@Configuration
@EnableJpaAuditing(auditorAwareRef = "agentGoAuditorAware")
class JpaAuditingConfiguration {
    @Bean
    fun agentGoAuditorAware(): AuditorAware<String> = AuditorAware {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated || authentication is AnonymousAuthenticationToken) {
            Optional.of("system")
        } else {
            Optional.of(authentication.name)
        }
    }
}
