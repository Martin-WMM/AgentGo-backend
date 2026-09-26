package com.agentgo.commons

data class ApiResponse<T>(
    val data: T,
    val requestId: String? = null,
)
