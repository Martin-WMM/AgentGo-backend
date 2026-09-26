package com.agentgo.dto

data class ApiResponse<T>(
    val data: T,
    val requestId: String? = null,
)
