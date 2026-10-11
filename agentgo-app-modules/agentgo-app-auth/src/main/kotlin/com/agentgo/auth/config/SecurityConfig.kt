package com.agentgo.auth.config

import com.agentgo.auth.properties.AuthProperties
import com.agentgo.commons.api.ApiPaths
import java.net.http.HttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.security.web.util.matcher.OrRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.web.client.RestClient
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
    private val authProperties: AuthProperties,
) {
    private val log = LoggerFactory.getLogger(SecurityConfig::class.java)
    /**
     * Authentik (uvicorn) mishandles Java's default HTTP/2 token POSTs: the form body is
     * dropped or mangled, so grant_type never arrives and login fails as Invalid credentials.
     * Force HTTP/1.1 and keep Spring Security's OAuth token message converters — a bare
     * RestClient lets Jackson build a broken OAuth2AccessTokenResponse (null
     * additionalParameters) and turns a successful Authentik token reply into a redirect loop.
     */
    @Bean
    fun authorizationCodeTokenResponseClient(): OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {
        val httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build()
        val restClient = RestClient.builder()
            .requestFactory(JdkClientHttpRequestFactory(httpClient))
            .configureMessageConverters { converters ->
                converters.addCustomConverter(FormHttpMessageConverter())
                converters.addCustomConverter(OAuth2AccessTokenResponseHttpMessageConverter())
            }
            .defaultStatusHandler(OAuth2ErrorResponseErrorHandler())
            .build()
        return RestClientAuthorizationCodeTokenResponseClient().apply {
            setRestClient(restClient)
        }
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        authorizationCodeTokenResponseClient: OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>,
    ): SecurityFilterChain {
        val oidcLogoutSuccessHandler = LogoutSuccessHandler { _, response, authentication ->
            // The ID token sid is a hash, not an Authentik session UUID, so the Core API
            // cannot end the browser session. Redirect the browser to RP-initiated logout.
            // Authentik requires id_token_hint whenever post_logout_redirect_uri is set
            // (OIDC certification). Without a session/token, skip Authentik and open the UI gate.
            val idTokenHint = (authentication?.principal as? OidcUser)?.idToken?.tokenValue
            if (idTokenHint.isNullOrBlank()) {
                response.sendRedirect(authProperties.signedOutUiUrl())
                return@LogoutSuccessHandler
            }
            response.sendRedirect(authentikEndSessionUri(idTokenHint))
        }

        http
            .cors(Customizer.withDefaults())
            .csrf { csrf ->
                csrf.ignoringRequestMatchers(
                    "${ApiPaths.AUTH}/logout",
                    "${ApiPaths.AUTH}/profile",
                    ApiPaths.V1_FILES_ANT,
                    ApiPaths.V1_MODELS_ANT,
                )
            }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(
                        "/actuator/health",
                        "/actuator/health/**",
                        "${ApiPaths.AUTH}/session",
                        "${ApiPaths.AUTH}/login",
                        "/oauth2/**",
                        "/login/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                    ).permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .tokenEndpoint { token ->
                        token.accessTokenResponseClient(authorizationCodeTokenResponseClient)
                    }
                    .defaultSuccessUrl(authProperties.uiBaseUrl, true)
                    // Surface failures in the UI auth gate instead of /login?error, which with an
                    // already-authenticated Authentik session can bounce authorize↔callback forever.
                    .failureHandler { _, response, exception ->
                        log.warn("OIDC login failed: {}", exception.message)
                        response.sendRedirect(authProperties.uiBaseUrl.trimEnd('/') + "/?authError=1")
                    }
            }
            .logout { logout ->
                logout
                    .logoutRequestMatcher(
                        OrRequestMatcher(
                            RequestMatcher { request ->
                                request.servletPath == "${ApiPaths.AUTH}/logout" &&
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

    /**
     * Builds the Authentik RP-initiated logout URL.
     *
     * When [idTokenHint] is present, includes `post_logout_redirect_uri` so Authentik can
     * return the browser to the UI after invalidation. Without a hint, omits that parameter
     * because Authentik rejects `post_logout_redirect_uri` alone as a malformed request.
     *
     * @param idTokenHint raw OIDC ID token from the authenticated session, or null
     * @return absolute Authentik end-session URL for a browser redirect
     */
    internal fun authentikEndSessionUri(idTokenHint: String? = null): String {
        val base = authProperties.authentikBrowserBaseUrl.trimEnd('/')
        val slug = authProperties.authentikApplicationSlug.trim().trim('/').ifBlank { "agentgo" }
        val clientId = URLEncoder.encode(
            authProperties.oidcClientId.trim().ifBlank { "agentgo" },
            StandardCharsets.UTF_8,
        )
        if (idTokenHint.isNullOrBlank()) {
            return "$base/application/o/$slug/end-session/?client_id=$clientId"
        }
        val postLogout = URLEncoder.encode(authProperties.signedOutUiUrl(), StandardCharsets.UTF_8)
        val hint = URLEncoder.encode(idTokenHint, StandardCharsets.UTF_8)
        return "$base/application/o/$slug/end-session/" +
            "?client_id=$clientId&post_logout_redirect_uri=$postLogout&id_token_hint=$hint"
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
