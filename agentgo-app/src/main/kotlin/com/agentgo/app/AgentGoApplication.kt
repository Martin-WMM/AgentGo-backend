package com.agentgo.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AgentGoApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<AgentGoApplication>(*args)
        }
    }
}
