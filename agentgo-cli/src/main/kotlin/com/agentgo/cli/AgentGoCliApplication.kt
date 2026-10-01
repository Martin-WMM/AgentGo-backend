package com.agentgo.cli

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AgentGoCliApplication

fun main(args: Array<String>) {
    runApplication<AgentGoCliApplication>(*args)
}
