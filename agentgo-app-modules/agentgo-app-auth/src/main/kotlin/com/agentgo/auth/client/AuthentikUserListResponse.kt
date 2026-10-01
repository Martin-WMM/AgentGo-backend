package com.agentgo.auth.client

data class AuthentikUserListResponse(
    val results: List<AuthentikUserResponse> = emptyList(),
)
