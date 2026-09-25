package com.agentgo.core

class GreetingService {
    fun greet(name: String): String {
        require(name.isNotBlank()) { "name must not be blank" }
        return "Hello, $name!"
    }
}
