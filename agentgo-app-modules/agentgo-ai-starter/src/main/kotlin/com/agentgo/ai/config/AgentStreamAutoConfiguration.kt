package com.agentgo.ai.config

import com.agentgo.ai.checkpoint.AgentCheckpointSaver
import com.agentgo.ai.checkpoint.AgentPostgresCheckpointSaverFactory
import com.agentgo.ai.controller.AgentStreamController
import com.agentgo.ai.controller.AgentStreamExceptionHandler
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
import com.agentgo.core.agent.AgentGoAgent
import com.agentgo.core.model.ChatModel
import com.agentgo.core.model.QwenChatModel
import com.agentgo.core.model.QwenClientConfig
import com.agentgo.core.model.UnavailableChatModel
import com.agentgo.core.runtime.AgentRunOptions
import com.agentgo.core.runtime.AgentRuntime
import com.agentgo.core.tool.RejectingToolExecutor
import com.agentgo.core.tool.ToolExecutor
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.sql.DataSource
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Auto-configuration for the agent SSE entry.
 *
 * Registers the stream worker pool, the runtime, and the HTTP adapter.
 */
@AutoConfiguration(
    afterName = [
        "com.agentgo.chat.config.ChatAutoConfiguration",
        "com.agentgo.cli.config.CliAutoConfiguration",
        "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
    ],
)
@EnableConfigurationProperties(AgentStreamProperties::class)
class AgentStreamAutoConfiguration {
    init {
        ExceptionResponseCodeRegistry.register<InvalidAgentStreamRequestException>(
            AgentResponseCodeRegistry.INVALID,
            AgentResponseCodeRegistry.INVALID_TYPE,
        )
        ExceptionResponseCodeRegistry.register<AgentStreamBusyException>(
            AgentResponseCodeRegistry.BUSY,
            AgentResponseCodeRegistry.BUSY_TYPE,
        )
    }

    /**
     * Selects the chat model for the agent runtime.
     *
     * A blank API key keeps the process available and fails the first model call.
     *
     * @param properties agent settings
     * @return configured Qwen client, or an unavailable model
     */
    @Bean
    fun agentChatModel(properties: AgentStreamProperties): ChatModel =
        if (properties.qwenApiKey.isBlank()) {
            UnavailableChatModel()
        } else {
            QwenChatModel(
                QwenClientConfig(
                    apiKey = properties.qwenApiKey,
                    modelId = properties.modelId,
                    baseUrl = properties.qwenBaseUrl,
                ),
            )
        }

    /**
     * Leak-safe PostgreSQL checkpoint saver for LangGraph thread state.
     *
     * The wrapper stays inside the bundle. Registering it as a [DataSource] bean
     * makes Flyway treat it as the application pool and cycles back into this method.
     *
     * @param dataSource application connection pool
     * @return saver plus release handle for post-run connection cleanup
     */
    @Bean
    fun agentCheckpointBundle(@Qualifier("dataSource") dataSource: DataSource): AgentCheckpointSaver =
        AgentPostgresCheckpointSaverFactory.create(dataSource)

    /**
     * LangGraph checkpoint saver bean used when the agent graph is compiled.
     *
     * @param bundle leak-safe saver bundle
     * @return durable saver
     */
    @Bean
    fun agentCheckpointSaver(bundle: AgentCheckpointSaver): BaseCheckpointSaver = bundle.saver

    /**
     * Tool boundary for the agent ReAct loop.
     *
     * When the CLI module is present, Bash calls are delegated to
     * [CliExecutionService] (same path as `POST /api/v1/cli/executions`).
     *
     * @param cliExecutionService optional CLI execution service
     * @return Bash-backed executor, or a rejecting stub when CLI is absent
     */
    @Bean
    fun agentToolExecutor(
        cliExecutionService: ObjectProvider<CliExecutionService>,
    ): ToolExecutor {
        val service = cliExecutionService.ifAvailable ?: return RejectingToolExecutor()
        return BashToolExecutor(service)
    }

    /**
     * Builds the runtime used by the stream service.
     *
     * @param chatModel planner and decision model
     * @param properties agent settings
     * @param checkpointSaver PostgreSQL saver for conversation checkpoints
     * @param toolExecutor side-effect boundary for tool / CLI calls
     * @param cliExecutionService optional CLI service; presence enables the Bash tool catalog
     * @return runtime that publishes shared event fields
     */
    @Bean
    fun agentRuntime(
        chatModel: ChatModel,
        properties: AgentStreamProperties,
        checkpointSaver: BaseCheckpointSaver,
        toolExecutor: ToolExecutor,
        cliExecutionService: ObjectProvider<CliExecutionService>,
    ): AgentRuntime {
        val tools =
            if (cliExecutionService.ifAvailable != null) {
                listOf(BashTool.SPEC)
            } else {
                emptyList()
            }
        return AgentGoAgent(
            chatModel = chatModel,
            toolExecutor = toolExecutor,
            options = AgentRunOptions(modelId = properties.modelId, tools = tools),
            checkpointSaver = checkpointSaver,
        ).runtime
    }

    /**
     * Worker pool for SSE writes.
     *
     * Threads are named `agentgo-agent-stream-`. Rejected tasks fail the request
     * before the response starts.
     *
     * @param properties pool size and queue capacity
     * @return bounded stream executor
     */
    @Bean(destroyMethod = "shutdown")
    fun agentStreamExecutor(properties: AgentStreamProperties): ExecutorService {
        require(properties.poolSize > 0) { "Agent stream pool size must be positive." }
        require(properties.queueCapacity > 0) { "Agent stream queue capacity must be positive." }
        val threadNumber = AtomicInteger()
        return ThreadPoolExecutor(
            properties.poolSize,
            properties.poolSize,
            0L,
            TimeUnit.MILLISECONDS,
            ArrayBlockingQueue(properties.queueCapacity),
            { runnable -> Thread(runnable, "agentgo-agent-stream-${threadNumber.incrementAndGet()}") },
            ThreadPoolExecutor.AbortPolicy(),
        )
    }

    /**
     * Stream service bean.
     *
     * @param runtime agent runtime
     * @param properties model id copied into each run
     * @param turnWriter optional chat-history persistence when the chat module is present
     * @param checkpointBundle optional saver whose datasource is released after each run
     * @return service that maps runtime events to SSE payloads
     */
    @Bean
    fun agentStreamService(
        runtime: AgentRuntime,
        properties: AgentStreamProperties,
        turnWriter: ObjectProvider<ChatSessionTurnWriter>,
        checkpointBundle: ObjectProvider<AgentCheckpointSaver>,
    ): AgentStreamServiceImpl =
        AgentStreamServiceImpl(
            runtime = runtime,
            modelId = properties.modelId,
            turnWriter = turnWriter.ifAvailable,
            checkpointDataSource = checkpointBundle.ifAvailable?.dataSource,
        )

    /**
     * HTTP adapter beans that are not discovered by component scan of the application package.
     *
     * @param service stream service
     * @param executor stream worker pool
     * @param properties SSE timeout
     * @return controller
     */
    @Bean
    fun agentStreamController(
        service: AgentStreamServiceImpl,
        executor: ExecutorService,
        properties: AgentStreamProperties,
    ): AgentStreamController = AgentStreamController(service, executor, properties)

    /**
     * Exception handler for failures raised before the SSE response starts.
     *
     * @param messages localized API message resolver
     * @return handler
     */
    @Bean
    fun agentStreamExceptionHandler(
        messages: com.agentgo.starter.i18n.AgentGoMessages,
    ): AgentStreamExceptionHandler = AgentStreamExceptionHandler(messages)
}
