package com.agentgo.core.model

import com.agentgo.core.agent.AgentGoAgent
import com.agentgo.core.runtime.AgentRunOptions
import kotlin.test.Test
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assumptions

/**
 * Live DashScope run. Skipped unless AGENTGO_AGENT_QWEN_API_KEY is set.
 */
class QwenAgentLiveTest {
    @Test
    fun `qwen agent answers a direct arithmetic question`() {
        val apiKey = System.getenv("AGENTGO_AGENT_QWEN_API_KEY").orEmpty()
        Assumptions.assumeTrue(apiKey.isNotBlank()) { "AGENTGO_AGENT_QWEN_API_KEY is not set." }
        val agent = AgentGoAgent(
            QwenChatModel(
                QwenClientConfig(
                    apiKey = apiKey,
                    modelId = QwenModels.DEFAULT_ID,
                ),
            ),
            options = AgentRunOptions(modelId = QwenModels.DEFAULT_ID, maxSteps = 1),
        )

        val result = agent.run("1+1 等于几？只回答一个阿拉伯数字。")

        println("AGENT_PLAN=${result.plan}")
        println("AGENT_OUTPUT=${result.output}")
        println("AGENT_TRACE=${result.trace}")
        assertTrue(result.output.contains("2"), result.output)
        assertTrue(result.trace.contains("input"))
        assertTrue(result.trace.contains("output"))
    }
}
