package com.agentgo.commons.api

/**
 * Supported URI API version segments for AgentGo HTTP paths.
 *
 * Business endpoints use these values under a versioned /api path.
 * Authentication endpoints stay unversioned.
 */
object ApiVersions {
    /** First product API surface. */
    const val V1: String = "v1"
}
