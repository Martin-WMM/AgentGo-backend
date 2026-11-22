package com.agentgo.core.agent

import com.agentgo.core.event.AgentEventPublisher
import com.agentgo.core.model.ChatModel
import com.agentgo.core.runtime.AgentNodeActions
import com.agentgo.core.runtime.AgentNodeIds
import com.agentgo.core.runtime.AgentRoutes
import com.agentgo.core.runtime.AgentRunOptions
import com.agentgo.core.runtime.AgentSessionState
import com.agentgo.core.runtime.ExecutionNode
import com.agentgo.core.runtime.InputNode
import com.agentgo.core.runtime.LlmNode
import com.agentgo.core.runtime.OutputNode
import com.agentgo.core.runtime.PlanNode
import com.agentgo.core.tool.ToolExecutor
import org.bsc.langgraph4j.CompileConfig
import org.bsc.langgraph4j.CompiledGraph
import org.bsc.langgraph4j.GraphDefinition
import org.bsc.langgraph4j.GraphStateException
import org.bsc.langgraph4j.StateGraph
import org.bsc.langgraph4j.action.AsyncEdgeAction
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver

/**
 * LangGraph for the AgentGo agent.
 *
 * The compiled path is Input, Plan (structured JSON), then the AgentLoop ReAct
 * cycle LLM ⇄ Execution until the model finishes or the step budget ends, then Output.
 *
 * @param chatModel planner and decision model
 * @param toolExecutor allow-listed tool boundary
 * @param options model id, step budget, and tools
 * @param events runtime that publishes shared event fields
 * @param checkpointSaver optional durable saver for thread checkpoints
 */
class AgentGoAgentGraph(
    chatModel: ChatModel,
    toolExecutor: ToolExecutor,
    private val options: AgentRunOptions,
    private val events: AgentEventPublisher,
    private val checkpointSaver: BaseCheckpointSaver? = null,
) {
    private val compiled: CompiledGraph<AgentSessionState> = compile(chatModel, toolExecutor)

    /** Compiled graph executed by [com.agentgo.core.runtime.AgentRuntime]. */
    fun compiled(): CompiledGraph<AgentSessionState> = compiled

    private fun compile(
        chatModel: ChatModel,
        toolExecutor: ToolExecutor,
    ): CompiledGraph<AgentSessionState> {
        val inputNode = InputNode(events)
        val planNode = PlanNode(chatModel, events)
        val llmNode = LlmNode(chatModel, options, events)
        val executionNode = ExecutionNode(toolExecutor, options.tools, events)
        val outputNode = OutputNode(events)
        val graph = StateGraph(AgentSessionState.SCHEMA, AgentSessionState.FACTORY)
            .addNode(AgentNodeIds.INPUT, AgentNodeActions.of(AgentNodeIds.INPUT) { inputNode.apply(it) })
            .addNode(AgentNodeIds.PLAN, AgentNodeActions.of(AgentNodeIds.PLAN) { planNode.apply(it) })
            .addNode(AgentNodeIds.LLM, AgentNodeActions.of(AgentNodeIds.LLM) { llmNode.apply(it) })
            .addNode(
                AgentNodeIds.EXECUTION,
                AgentNodeActions.of(AgentNodeIds.EXECUTION) { executionNode.apply(it) },
            )
            .addNode(AgentNodeIds.OUTPUT, AgentNodeActions.of(AgentNodeIds.OUTPUT) { outputNode.apply(it) })
            .addEdge(GraphDefinition.START, AgentNodeIds.INPUT)
            .addConditionalEdges(
                AgentNodeIds.INPUT,
                AsyncEdgeAction.edge_async { state: AgentSessionState ->
                    if (state.route() == AgentRoutes.FINISH) AgentNodeIds.OUTPUT else AgentNodeIds.PLAN
                },
                mapOf(
                    AgentNodeIds.OUTPUT to AgentNodeIds.OUTPUT,
                    AgentNodeIds.PLAN to AgentNodeIds.PLAN,
                ),
            )
            .addConditionalEdges(
                AgentNodeIds.PLAN,
                AsyncEdgeAction.edge_async { state: AgentSessionState ->
                    if (state.route() == AgentRoutes.FINISH) AgentNodeIds.OUTPUT else AgentNodeIds.LLM
                },
                mapOf(
                    AgentNodeIds.OUTPUT to AgentNodeIds.OUTPUT,
                    AgentNodeIds.LLM to AgentNodeIds.LLM,
                ),
            )
            .addConditionalEdges(
                AgentNodeIds.LLM,
                AsyncEdgeAction.edge_async { state: AgentSessionState ->
                    if (state.route() == AgentRoutes.EXECUTE) AgentNodeIds.EXECUTION else AgentNodeIds.OUTPUT
                },
                mapOf(
                    AgentNodeIds.EXECUTION to AgentNodeIds.EXECUTION,
                    AgentNodeIds.OUTPUT to AgentNodeIds.OUTPUT,
                ),
            )
            .addEdge(AgentNodeIds.EXECUTION, AgentNodeIds.LLM)
            .addEdge(AgentNodeIds.OUTPUT, GraphDefinition.END)
        val compileConfig = CompileConfig.builder()
            .recursionLimit(options.maxSteps * 2 + 8)
            .graphId("agentgo-agent")
        if (checkpointSaver != null) {
            compileConfig.checkpointSaver(checkpointSaver)
        }
        return try {
            graph.compile(compileConfig.build())
        } catch (ex: GraphStateException) {
            throw IllegalStateException("AgentGo agent graph failed to compile.", ex)
        }
    }
}
