package com.agentgo.core

class DefaultAgentWorkflow : AgentWorkflow {
    override fun execute(input: String): String = input.trim()
}
