package com.agentgo.ai.checkpoint

import com.agentgo.core.runtime.AgentSessionState
import javax.sql.DataSource
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver
import org.bsc.langgraph4j.checkpoint.PostgresSaver
import org.bsc.langgraph4j.serializer.std.ObjectStreamStateSerializer

/**
 * Builds the LangGraph4j PostgreSQL checkpoint saver used by the AgentGo agent.
 */
object AgentPostgresCheckpointSaverFactory {
    /**
     * Creates a [PostgresSaver] bound to a leak-safe wrapper around [dataSource].
     *
     * Table DDL is owned by Flyway (`V19__langgraph4j_postgres_saver.sql`), so
     * the saver does not create or drop tables at runtime.
     *
     * langgraph4j 1.9.1 `AbstractPostgresSaver.exec()` borrows JDBC connections
     * without closing them. [CheckpointDataSource] reuses one connection per
     * thread and exposes [CheckpointDataSource.releaseAll] for post-run cleanup.
     *
     * @param dataSource shared AgentGo PostgreSQL datasource
     * @return durable checkpoint saver plus the release handle
     */
    fun create(dataSource: DataSource): AgentCheckpointSaver {
        val checkpointDataSource = CheckpointDataSource(dataSource)
        val saver = PostgresSaver.builder()
            .datasource(checkpointDataSource)
            .stateSerializer(ObjectStreamStateSerializer(AgentSessionState.FACTORY))
            .createTables(false)
            .dropTablesFirst(false)
            .build()
        return AgentCheckpointSaver(saver, checkpointDataSource)
    }
}

/**
 * Pair of the LangGraph checkpoint saver and its connection-release handle.
 *
 * @property saver saver passed into the compiled agent graph
 * @property dataSource leak-safe datasource that must be released after each run
 */
class AgentCheckpointSaver(
    val saver: BaseCheckpointSaver,
    val dataSource: CheckpointDataSource,
)
