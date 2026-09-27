package com.agentgo.auth.client

import com.agentgo.auth.properties.AuthProperties
import com.agentgo.dto.auth.ProfileUpdateRequest
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.util.UriComponentsBuilder

@Component
class AuthentikProfileClientImpl(
    private val authProperties: AuthProperties,
) : AuthentikProfileClient {
    private val json = ObjectMapper()
    private val restClient: RestClient = RestClient.builder()
        .baseUrl(authProperties.authentikApiBaseUrl.trimEnd('/'))
        .defaultHeaders { headers ->
            headers.accept = listOf(MediaType.APPLICATION_JSON)
            headers.setBearerAuth(authProperties.authentikApiToken)
        }
        .build()

    override fun findBySubject(subject: String, email: String?): AuthentikUserResponse {
        requireConfigured()
        if (isAuthenticatedSessionUuid(subject)) {
            return requireSingleUser(usersBy("uuid", subject))
        }
        val normalizedEmail = email?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException("The authenticated user was not found in Authentik")
        return requireSingleUser(usersBy("email", normalizedEmail))
    }

    override fun update(user: AuthentikUserResponse, request: ProfileUpdateRequest): AuthentikUserResponse {
        requireConfigured()
        val body = restClient.patch()
            .uri("/api/v3/core/users/{id}/", user.pk)
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                mapOf(
                    "name" to request.displayName.trim(),
                    "email" to request.email?.trim()?.ifBlank { null },
                    "avatar" to request.avatarUrl?.trim()?.ifBlank { null },
                ),
            )
            .retrieve()
            .body(String::class.java)
            ?: throw IllegalStateException("Authentik returned an empty profile response")
        return toUser(json.readTree(body))
    }

    override fun terminateSession(sessionId: String) {
        // The OIDC sid is not an authenticated-session UUID, so there is no Core API
        // resource to delete. Logout still clears the local Spring session.
        if (!isAuthenticatedSessionUuid(sessionId)) return
        requireConfigured()
        restClient.delete()
            .uri("/api/v3/core/authenticated_sessions/{sessionId}/", sessionId)
            .retrieve()
            .toBodilessEntity()
    }

    override fun isSessionActive(sessionId: String): Boolean {
        // Authentik's OIDC sid is sha256(session_key), while this API retrieves sessions
        // by UUID. A hash always misses. Callers must not treat that miss as logout, or the
        // UI session check fails after every successful login and refresh.
        if (!isAuthenticatedSessionUuid(sessionId)) return true
        requireConfigured()
        return try {
            restClient.get()
                .uri("/api/v3/core/authenticated_sessions/{sessionId}/", sessionId)
                .retrieve()
                .toBodilessEntity()
            true
        } catch (exception: RestClientResponseException) {
            if (exception.statusCode.value() == 404) false else throw exception
        }
    }

    private fun usersBy(parameter: String, value: String): AuthentikUserListResponse {
        // Encode the query so an email "@" is not treated as URI user-info. Read the JSON
        // tree directly; the default mapper drops Kotlin constructor values and looks empty.
        val uri = UriComponentsBuilder
            .fromUriString(authProperties.authentikApiBaseUrl.trimEnd('/'))
            .path("/api/v3/core/users/")
            .queryParam(parameter, value)
            .encode()
            .build()
            .toUri()
        val raw = restClient.get().uri(uri).retrieve().body(String::class.java).orEmpty()
        val results = json.readTree(raw).path("results")
        if (!results.isArray) {
            return AuthentikUserListResponse()
        }
        return AuthentikUserListResponse(results.map { node -> toUser(node) })
    }

    private fun toUser(node: JsonNode): AuthentikUserResponse {
        val email = textOrNull(node, "email")
        val avatar = textOrNull(node, "avatar")
        return AuthentikUserResponse(
            pk = node.path("pk").asLong(),
            username = node.path("username").asText(""),
            name = node.path("name").asText(""),
            email = email,
            avatar = avatar,
            uuid = UUID.fromString(node.path("uuid").asText()),
        )
    }

    private fun textOrNull(node: JsonNode, field: String): String? {
        val value = node.path(field).asText("")
        return value.ifBlank { null }
    }

    private fun requireSingleUser(response: AuthentikUserListResponse): AuthentikUserResponse =
        response.results.singleOrNull()
            ?: throw IllegalStateException("The authenticated user was not found in Authentik")

    private fun isAuthenticatedSessionUuid(value: String): Boolean =
        value.length == 36 && runCatching { UUID.fromString(value) }.isSuccess

    private fun requireConfigured() {
        check(authProperties.authentikApiToken.isNotBlank()) {
            "Authentik profile synchronization is not configured"
        }
    }
}
