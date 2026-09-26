package com.agentgo.core

interface AgentWorkflow {
    fun execute(input: String): String
}

class DefaultAgentWorkflow : AgentWorkflow {
    override fun execute(input: String): String = input.trim()
}
