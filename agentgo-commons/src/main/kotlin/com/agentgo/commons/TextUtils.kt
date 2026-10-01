package com.agentgo.commons

object TextUtils {
    fun requireText(value: String, field: String): String {
        require(value.isNotBlank()) { "$field must not be blank" }
        return value.trim()
    }
}
