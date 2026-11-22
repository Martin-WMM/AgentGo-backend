package com.agentgo.ai.checkpoint

import java.sql.Connection
import java.util.concurrent.atomic.AtomicInteger
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CheckpointDataSourceTest {
    @Test
    fun `reuses one open connection per thread until closed`() {
        val opens = AtomicInteger()
        val target = DataSource { opens.incrementAndGet(); FakeConnection() }
        val dataSource = CheckpointDataSource(target)

        val first = dataSource.connection
        val second = dataSource.connection

        assertSame(first, second)
        assertEquals(1, opens.get())
        first.close()
        assertTrue(first.isClosed)
    }

    @Test
    fun `releaseAll closes tracked connections`() {
        val target = DataSource { FakeConnection() }
        val dataSource = CheckpointDataSource(target)
        val borrowed = dataSource.connection

        assertFalse(borrowed.isClosed)
        dataSource.releaseAll()
        assertTrue(borrowed.isClosed)
    }

    @Test
    fun `close after releaseAll does not throw`() {
        val target = DataSource { FakeConnection() }
        val dataSource = CheckpointDataSource(target)
        val borrowed = dataSource.connection

        dataSource.releaseAll()
        borrowed.close()
        assertTrue(borrowed.isClosed)
    }

    private fun interface DataSource : javax.sql.DataSource {
        fun open(): Connection

        override fun getConnection(): Connection = open()

        override fun getConnection(username: String?, password: String?): Connection = open()

        override fun getLogWriter() = throw UnsupportedOperationException()

        override fun setLogWriter(out: java.io.PrintWriter?) = Unit

        override fun setLoginTimeout(seconds: Int) = Unit

        override fun getLoginTimeout() = 0

        override fun getParentLogger() = java.util.logging.Logger.getLogger("global")

        override fun <T : Any?> unwrap(iface: Class<T>): T = throw UnsupportedOperationException()

        override fun isWrapperFor(iface: Class<*>) = false
    }

    private class FakeConnection : Connection by unsupportedConnection() {
        private var closed = false

        override fun isClosed(): Boolean = closed

        override fun close() {
            closed = true
        }

        override fun equals(other: Any?): Boolean = this === other

        override fun hashCode(): Int = System.identityHashCode(this)
    }

    private companion object {
        fun unsupportedConnection(): Connection =
            java.lang.reflect.Proxy.newProxyInstance(
                Connection::class.java.classLoader,
                arrayOf(Connection::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "toString" -> "unsupported"
                    "hashCode" -> 0
                    "equals" -> false
                    else -> throw UnsupportedOperationException(method.name)
                }
            } as Connection
    }
}
