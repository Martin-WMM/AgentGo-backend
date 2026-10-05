package com.agentgo.auth.config

import com.agentgo.auth.properties.AuthProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.security.web.util.matcher.OrRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
    private val authProperties: AuthProperties,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val oidcLogoutSuccessHandler = LogoutSuccessHandler { _, response, _ ->
            // The ID token sid is a hash, not an Authentik session UUID, so the Core API
            // cannot end the browser session. Redirect the browser to RP-initiated logout.
            // The invalidation flow ends the Authentik session and returns to the UI.
            response.sendRedirect(authentikEndSessionUri())
        }

        http
            .cors(Customizer.withDefaults())
            .csrf { csrf ->
                csrf.ignoringRequestMatchers(
                    "/api/auth/logout",
                    "/api/auth/profile",
                    "/api/files/**",
                )
            }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(
                        "/actuator/health",
                        "/actuator/health/**",
                        "/api/auth/session",
                        "/api/auth/login",
                        "/oauth2/**",
                        "/login/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                    ).permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 -> oauth2.defaultSuccessUrl(authProperties.uiBaseUrl, true) }
            .logout { logout ->
                logout
                    .logoutRequestMatcher(
                        OrRequestMatcher(
                            RequestMatcher { request ->
                                request.servletPath == "/api/auth/logout" &&
                                    request.method in setOf(HttpMethod.GET.name(), HttpMethod.POST.name())
                            },
                        ),
                    )
                    .logoutSuccessHandler(oidcLogoutSuccessHandler)
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
            }

        return http.build()
    }

    private fun authentikEndSessionUri(): String {
        val base = authProperties.authentikBrowserBaseUrl.trimEnd('/')
        val slug = authProperties.authentikApplicationSlug.trim().trim('/').ifBlank { "agentgo" }
        return "$base/application/o/$slug/end-session/"
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf(authProperties.uiBaseUrl)
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}
