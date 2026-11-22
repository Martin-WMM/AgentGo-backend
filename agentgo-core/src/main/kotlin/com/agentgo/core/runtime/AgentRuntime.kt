package com.agentgo.core.runtime

import com.agentgo.core.event.AgentEvent
import com.agentgo.core.event.AgentEventDraft
import com.agentgo.core.event.AgentEventKind
import com.agentgo.core.event.AgentEventPublisher
import com.agentgo.core.event.AgentLifecyclePhase
import com.agentgo.core.json.AgentJson
import java.util.HashMap
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bsc.langgraph4j.CompiledGraph
import org.bsc.langgraph4j.RunnableConfig
import org.slf4j.LoggerFactory

/**
 * Executes a compiled AgentGo graph and publishes its event stream.
 *
 * Nodes call [publish] with the run id stored in graph state. This runtime copies
 * the run's thread id, header parameters, and metadata onto every event.
 * The run is kept in a map because graph work is not confined to the caller thread.
 */
class AgentRuntime : AgentEventPublisher {
    private val log = LoggerFactory.getLogger(AgentRuntime::class.java)
    private lateinit var graph: CompiledGraph<AgentSessionState>
    private val runs = ConcurrentHashMap<String, ActiveRun>()

    /**
     * Attaches the compiled graph. Called once after the nodes that publish
     * through this runtime have been created.
     *
     * @param compiled compiled AgentGo graph
     */
    fun attach(compiled: CompiledGraph<AgentSessionState>) {
        graph = compiled
    }

    /**
     * Runs the graph and emits events as nodes publish them.
     *
     * The terminal event is an agent lifecycle event with [AgentEvent.finished] set.
     *
     * @param command run input, thread, headers, and metadata
     * @param emit consumer invoked once per event on the calling thread
     */
    fun stream(command: AgentStreamCommand, emit: (AgentEvent) -> Unit) {
        require(command.threadId.isNotBlank()) { "threadId must not be blank." }
        val runId = UUID.randomUUID().toString()
        runs[runId] = ActiveRun(
            threadId = command.threadId,
            headers = command.headers.toMap(),
            metadata = command.metadata.toMap(),
            emit = emit,
        )
        log.info(
            "agent.run.start runId={} threadId={} modelId={}",
            runId,
            command.threadId,
            command.modelId,
        )
        try {
            publish(runId, AgentEventDraft.agent(AgentLifecyclePhase.STARTED))
            val state = graph.invoke(initialState(command, runId), runnableConfig(command)).orElse(null)
            publish(
                runId,
                AgentEventDraft.agent(
                    phase = AgentLifecyclePhase.COMPLETED,
                    content = state?.output().orEmpty(),
                    finished = true,
                ),
            )
            log.info(
                "agent.run.completed runId={} threadId={} outputChars={}",
                runId,
                command.threadId,
                state?.output().orEmpty().length,
            )
        } catch (ex: Exception) {
            publish(
                runId,
                AgentEventDraft.agent(
                    phase = AgentLifecyclePhase.FAILED,
                    content = ex.message ?: "Agent run failed.",
                    finished = true,
                ),
            )
            log.error(
                "agent.run.failed runId={} threadId={}",
                runId,
                command.threadId,
                ex,
            )
        } finally {
            runs.remove(runId)
        }
    }

    /**
     * Publishes [draft] for [runId].
     *
     * @param runId id of the active run stored in graph state
     * @param draft event authored by a node or by this runtime
     */
    override fun publish(runId: String, draft: AgentEventDraft) {
        val run = runs[runId] ?: throw IllegalStateException("Agent event published outside a run.")
        logPublishedEvent(runId, run.threadId, draft)
        run.emit(
            AgentEvent(
                kind = draft.kind,
                name = draft.kind.eventName,
                phase = draft.phase,
                threadId = run.threadId,
                nodeId = draft.nodeId,
                content = draft.content,
                toolName = draft.toolName,
                headers = run.headers,
                metadata = run.metadata,
                finished = draft.finished,
            ),
        )
    }

    private fun logPublishedEvent(runId: String, threadId: String, draft: AgentEventDraft) {
        val preview = draft.content?.take(CONTENT_LOG_LIMIT)
        when (draft.kind) {
            AgentEventKind.NODE_LIFECYCLE ->
                log.info(
                    "agent.node.event runId={} threadId={} nodeId={} phase={} content={}",
                    runId,
                    threadId,
                    draft.nodeId,
                    draft.phase,
                    preview,
                )
            AgentEventKind.TOOL_LIFECYCLE, AgentEventKind.CLI_LIFECYCLE ->
                log.info(
                    "agent.tool.event runId={} threadId={} nodeId={} toolName={} kind={} phase={} content={}",
                    runId,
                    threadId,
                    draft.nodeId,
                    draft.toolName,
                    draft.kind.eventName,
                    draft.phase,
                    preview,
                )
            AgentEventKind.MESSAGE ->
                log.debug(
                    "agent.message.event runId={} threadId={} nodeId={} phase={} contentChars={}",
                    runId,
                    threadId,
                    draft.nodeId,
                    draft.phase,
                    draft.content?.length ?: 0,
                )
            AgentEventKind.AGENT_LIFECYCLE ->
                log.info(
                    "agent.lifecycle.event runId={} threadId={} phase={} finished={} content={}",
                    runId,
                    threadId,
                    draft.phase,
                    draft.finished,
                    preview,
                )
        }
    }

    private fun runnableConfig(command: AgentStreamCommand): RunnableConfig {
        val builder = RunnableConfig.builder().threadId(command.threadId)
        command.metadata.forEach { (key, value) -> builder.putMetadata(key, value) }
        return builder.build()
    }

    private fun initialState(command: AgentStreamCommand, runId: String): Map<String, Any> =
        HashMap<String, Any>().apply {
            put(AgentSessionState.INPUT, command.input)
            put(AgentSessionState.MODEL_ID, command.modelId)
            put(AgentSessionState.RUN_ID, runId)
            put(AgentSessionState.THREAD_ID, command.threadId)
            put(AgentSessionState.HEADERS, AgentJson.writeArguments(command.headers))
            put(AgentSessionState.METADATA, AgentJson.writeArguments(command.metadata))
        }

    private class ActiveRun(
        val threadId: String,
        val headers: Map<String, String>,
        val metadata: Map<String, String>,
        val emit: (AgentEvent) -> Unit,
    )

    companion object {
        private const val CONTENT_LOG_LIMIT: Int = 200
    }
}
