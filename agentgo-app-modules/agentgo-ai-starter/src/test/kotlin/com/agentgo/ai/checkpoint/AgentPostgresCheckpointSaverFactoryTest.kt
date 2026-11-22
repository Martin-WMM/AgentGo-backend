package com.agentgo.ai.checkpoint

import java.sql.DriverManager
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.postgresql.ds.PGSimpleDataSource

class AgentPostgresCheckpointSaverFactoryTest {
    @Test
    fun `create fails when the datasource cannot open a connection`() {
        val dataSource = mock<DataSource> {
            on { connection } doAnswer { throw IllegalStateException("no database") }
        }

        assertFails {
            AgentPostgresCheckpointSaverFactory.create(dataSource)
        }
    }

    @Test
    fun `create builds a postgres saver against the local agentgo database`() {
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
        ensureCheckpointTables(dataSource)

        val bundle = AgentPostgresCheckpointSaverFactory.create(dataSource)

        assertNotNull(bundle.saver)
        assertNotNull(bundle.dataSource)
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

    private fun ensureCheckpointTables(dataSource: DataSource) {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS LG4JThread (
                        thread_id UUID PRIMARY KEY,
                        thread_name VARCHAR(255),
                        is_released BOOLEAN DEFAULT FALSE NOT NULL
                    )
                    """.trimIndent(),
                )
                statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS LG4JCheckpoint (
                        checkpoint_id UUID PRIMARY KEY,
                        parent_checkpoint_id UUID,
                        thread_id UUID NOT NULL,
                        node_id VARCHAR(255),
                        next_node_id VARCHAR(255),
                        state_data JSONB NOT NULL,
                        state_content_type VARCHAR(100) NOT NULL,
                        saved_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}
