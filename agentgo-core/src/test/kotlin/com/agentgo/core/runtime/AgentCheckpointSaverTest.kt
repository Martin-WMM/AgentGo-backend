package com.agentgo.core.runtime

import com.agentgo.core.agent.AgentGoAgent
import com.agentgo.core.event.AgentEvent
import com.agentgo.core.event.AgentEventKind
import com.agentgo.core.event.AgentLifecyclePhase
import com.agentgo.core.model.ChatModel
import com.agentgo.core.model.ChatRequest
import com.agentgo.core.model.ChatResponse
import org.bsc.langgraph4j.checkpoint.MemorySaver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AgentCheckpointSaverTest {
    @Test
    fun `stream completes when the graph uses a memory checkpoint saver`() {
        val agent = AgentGoAgent(
            chatModel = ScriptedChatModel(
                listOf(
                    "1. Answer the user.",
                    """{"type":"final","content":"checkpoint-ok"}""",
                ),
            ),
            checkpointSaver = MemorySaver(),
        )
        val events = mutableListOf<AgentEvent>()

        agent.stream(
            AgentStreamCommand(
                input = "ping",
                modelId = "qwen3.7-plus",
                threadId = "thread-checkpoint-1",
                headers = emptyMap(),
                metadata = emptyMap(),
            ),
            events::add,
        )

        assertEquals(AgentLifecyclePhase.COMPLETED.name, events.last().phase)
        assertTrue(events.last().finished)
        assertEquals("checkpoint-ok", events.last().content)
        assertTrue(events.any { it.kind == AgentEventKind.NODE_LIFECYCLE && it.nodeId == AgentNodeIds.OUTPUT })
    }

    private class ScriptedChatModel(
        private val replies: List<String>,
    ) : ChatModel {
        private var index = 0

        override fun complete(request: ChatRequest): ChatResponse {
            val content = replies[index.coerceAtMost(replies.lastIndex)]
            index += 1
            return ChatResponse(content, request.modelId)
        }
    }
}
