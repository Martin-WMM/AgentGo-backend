package com.agentgo.cli

import org.springframework.shell.core.command.annotation.Command
import org.springframework.stereotype.Component

@Component
class AgentGoCommands {
    @Command(name = ["agentgo", "version"], description = "Show the AgentGo CLI version")
    fun version(): String = "AgentGo CLI 0.1.0"
}
