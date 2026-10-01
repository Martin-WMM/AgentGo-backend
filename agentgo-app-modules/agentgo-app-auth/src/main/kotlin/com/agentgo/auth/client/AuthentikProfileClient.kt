package com.agentgo.auth.client

import com.agentgo.dto.auth.ProfileUpdateRequest

interface AuthentikProfileClient {
    /**
     * Finds the Authentik user for an OIDC subject.
     *
     * A UUID subject is looked up directly. Authentik's default subject is a hashed user id,
     * which the Core API cannot query, so [email] is used in that case.
     */
    fun findBySubject(subject: String, email: String? = null): AuthentikUserResponse

    fun update(user: AuthentikUserResponse, request: ProfileUpdateRequest): AuthentikUserResponse

    /**
     * Terminates an Authentik browser session.
     *
     * [sessionId] must be the authenticated-session UUID. An OIDC `sid` is a hash of the
     * session key and is ignored, because the Core API cannot address a session by that hash.
     */
    fun terminateSession(sessionId: String)

    /**
     * Returns whether an Authentik browser session is still active.
     *
     * Only authenticated-session UUIDs can be checked. An OIDC `sid` is not that UUID, so it
     * is treated as still active instead of as a logged-out session.
     */
    fun isSessionActive(sessionId: String): Boolean
}
