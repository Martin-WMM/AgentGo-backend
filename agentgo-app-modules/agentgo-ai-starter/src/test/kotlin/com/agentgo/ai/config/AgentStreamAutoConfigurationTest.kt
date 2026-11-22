package com.agentgo.ai.config

import com.agentgo.ai.checkpoint.AgentCheckpointSaver
import com.agentgo.ai.exception.AgentStreamBusyException
import com.agentgo.ai.exception.InvalidAgentStreamRequestException
import com.agentgo.ai.properties.AgentStreamProperties
import com.agentgo.ai.service.ChatSessionTurnWriter
import com.agentgo.ai.service.impl.AgentStreamServiceImpl
import com.agentgo.ai.tool.BashTool
import com.agentgo.ai.tool.BashToolExecutor
import com.agentgo.cli.service.CliExecutionService
import com.agentgo.commons.dto.http.response.status.error.AgentResponseCodeRegistry
import com.agentgo.commons.dto.http.response.status.error.ExceptionResponseCodeRegistry
import com.agentgo.core.model.QwenChatModel
import com.agentgo.core.model.QwenModels
import com.agentgo.core.model.UnavailableChatModel
import com.agentgo.core.runtime.AgentRuntime
import com.agentgo.core.tool.RejectingToolExecutor
import java.sql.DriverManager
import java.util.concurrent.ThreadPoolExecutor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import org.bsc.langgraph4j.checkpoint.MemorySaver
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.mockito.kotlin.mock
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.beans.factory.ObjectProvider

class AgentStreamAutoConfigurationTest {
    @Test
    fun `configuration registers agent stream exception codes`() {
        AgentStreamAutoConfiguration()

        assertEquals(
            AgentResponseCodeRegistry.INVALID,
            ExceptionResponseCodeRegistry.responseCode(InvalidAgentStreamRequestException::class)?.code,
        )
        assertEquals(
            AgentResponseCodeRegistry.BUSY_TYPE,
            ExceptionResponseCodeRegistry.responseCode(AgentStreamBusyException::class)?.responseType,
        )
    }

    @Test
    fun `blank api key selects the unavailable chat model`() {
        val model = AgentStreamAutoConfiguration().agentChatModel(AgentStreamProperties())

        assertIs<UnavailableChatModel>(model)
    }

    @Test
    fun `api key selects the qwen chat model`() {
        val properties = AgentStreamProperties().apply { qwenApiKey = "test-key" }

        assertIs<QwenChatModel>(AgentStreamAutoConfiguration().agentChatModel(properties))
    }

    @Test
    fun `stream executor uses the configured pool size`() {
        val executor = AgentStreamAutoConfiguration().agentStreamExecutor(AgentStreamProperties())
        val pool = executor as ThreadPoolExecutor

        assertEquals(4, pool.corePoolSize)
        assertEquals(32, pool.queue.remainingCapacity())
        executor.shutdown()
    }

    @Test
    fun `stream executor rejects a non positive pool size`() {
        val properties = AgentStreamProperties().apply { poolSize = 0 }

        assertFailsWith<IllegalArgumentException> {
            AgentStreamAutoConfiguration().agentStreamExecutor(properties)
        }
    }

    @Test
    fun `stream executor rejects a non positive queue capacity`() {
        val properties = AgentStreamProperties().apply { queueCapacity = 0 }

        assertFailsWith<IllegalArgumentException> {
            AgentStreamAutoConfiguration().agentStreamExecutor(properties)
        }
    }

    @Test
    fun `agent tool executor rejects tools when CLI is absent`() {
        val cli = mock<ObjectProvider<CliExecutionService>> {
            on { ifAvailable }.thenReturn(null)
        }

        assertIs<RejectingToolExecutor>(AgentStreamAutoConfiguration().agentToolExecutor(cli))
    }

    @Test
    fun `agent tool executor uses Bash when CLI is present`() {
        val service = mock<CliExecutionService>()
        val cli = mock<ObjectProvider<CliExecutionService>> {
            on { ifAvailable }.thenReturn(service)
        }

        assertIs<BashToolExecutor>(AgentStreamAutoConfiguration().agentToolExecutor(cli))
    }

    @Test
    fun `agent runtime uses the configured model and checkpoint saver`() {
        val properties = AgentStreamProperties().apply { modelId = QwenModels.DEFAULT_ID }
        val cli = mock<ObjectProvider<CliExecutionService>> {
            on { ifAvailable }.thenReturn(null)
        }
        val runtime = AgentStreamAutoConfiguration().agentRuntime(
            chatModel = UnavailableChatModel(),
            properties = properties,
            checkpointSaver = MemorySaver(),
            toolExecutor = RejectingToolExecutor(),
            cliExecutionService = cli,
        )

        assertIs<AgentRuntime>(runtime)
    }

    @Test
    fun `agent runtime registers the Bash tool when CLI is present`() {
        val service = mock<CliExecutionService>()
        val cli = mock<ObjectProvider<CliExecutionService>> {
            on { ifAvailable }.thenReturn(service)
        }

        val runtime = AgentStreamAutoConfiguration().agentRuntime(
            chatModel = UnavailableChatModel(),
            properties = AgentStreamProperties(),
            checkpointSaver = MemorySaver(),
            toolExecutor = BashToolExecutor(service),
            cliExecutionService = cli,
        )

        assertIs<AgentRuntime>(runtime)
        assertEquals(BashTool.NAME, BashTool.SPEC.name)
    }

    @Test
    fun `agent stream service accepts optional turn writer and checkpoint bundle`() {
        val cli = mock<ObjectProvider<CliExecutionService>> {
            on { ifAvailable }.thenReturn(null)
        }
        val runtime = AgentStreamAutoConfiguration().agentRuntime(
            chatModel = UnavailableChatModel(),
            properties = AgentStreamProperties(),
            checkpointSaver = MemorySaver(),
            toolExecutor = RejectingToolExecutor(),
            cliExecutionService = cli,
        )
        val turnWriter = mock<ObjectProvider<ChatSessionTurnWriter>> {
            on { ifAvailable }.thenReturn(null)
        }
        val checkpointBundle = mock<ObjectProvider<AgentCheckpointSaver>> {
            on { ifAvailable }.thenReturn(null)
        }

        val service = AgentStreamAutoConfiguration().agentStreamService(
            runtime,
            AgentStreamProperties(),
            turnWriter,
            checkpointBundle,
        )

        assertIs<AgentStreamServiceImpl>(service)
    }

    @Test
    fun `checkpoint saver bean binds the shared datasource`() {
        assumeTrue(localPostgresReachable()) {
            "Local AgentGo PostgreSQL on 127.0.0.1:5433 is required."
        }
        val dataSource = PGSimpleDataSource().apply {
            serverNames = arrayOf("127.0.0.1")
            portNumbers = intArrayOf(5433)
            databaseName = "agentgo"
            user = "agentgo"
            password = "agentgo-local"
        }

        val bundle = AgentStreamAutoConfiguration().agentCheckpointBundle(dataSource)

        assertNotNull(bundle.saver)
        bundle.dataSource.releaseAll()
    }

    private fun localPostgresReachable(): Boolean =
        try {
            DriverManager.getConnection(
                "jdbc:postgresql://127.0.0.1:5433/agentgo",
                "agentgo",
                "agentgo-local",
            ).use { true }
        } catch (_: Exception) {
            false
        }
}
