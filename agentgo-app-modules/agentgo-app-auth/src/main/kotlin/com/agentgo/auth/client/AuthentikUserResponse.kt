package com.agentgo.auth.client

import java.util.UUID

data class AuthentikUserResponse(
    val pk: Long,
    val username: String,
    val name: String,
    val email: String?,
    val avatar: String?,
    val uuid: UUID,
)
