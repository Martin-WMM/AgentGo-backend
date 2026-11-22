package com.agentgo.ai.checkpoint

import java.io.PrintWriter
import java.sql.Connection
import java.sql.SQLException
import java.util.logging.Logger
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CheckpointDataSourceCoverageTest {
    @Test
    fun `covers datasource metadata and wrapper helpers`() {
        val raw = FakeConnection()
        val target = object : DataSource {
            var writer: PrintWriter? = null
            var timeout = 0
            override fun getConnection(): Connection = raw
            override fun getConnection(username: String?, password: String?): Connection = raw
            override fun getLogWriter(): PrintWriter? = writer
            override fun setLogWriter(out: PrintWriter?) {
                writer = out
            }
            override fun setLoginTimeout(seconds: Int) {
                timeout = seconds
            }
            override fun getLoginTimeout(): Int = timeout
            override fun getParentLogger(): Logger = Logger.getLogger("global")
            override fun <T : Any?> unwrap(iface: Class<T>): T {
                if (iface.isInstance(this)) return iface.cast(this)
                throw SQLException("unwrap")
            }
            override fun isWrapperFor(iface: Class<*>): Boolean = iface.isInstance(this)
        }
        val dataSource = CheckpointDataSource(target)
        val writer = PrintWriter(System.out)
        dataSource.logWriter = writer
        assertSame(writer, dataSource.logWriter)
        dataSource.loginTimeout = 7
        assertEquals(7, dataSource.loginTimeout)
        assertEquals("global", dataSource.parentLogger.name)
        assertTrue(dataSource.isWrapperFor(CheckpointDataSource::class.java))
        assertSame(dataSource, dataSource.unwrap(CheckpointDataSource::class.java))
        assertFailsWith<SQLException> { dataSource.createConnectionBuilder() }

        val first = dataSource.connection
        val second = dataSource.getConnection("u", "p")
        assertSame(first, second)
        assertTrue(first.isWrapperFor(Connection::class.java))
        assertSame(raw, first.unwrap(Connection::class.java))
        assertEquals(first, first)
        assertFalse(first == Any())
        first.hashCode()
        first.close()
        assertTrue(raw.closed)
    }

    private class FakeConnection : Connection by unsupported() {
        var closed = false
        override fun isClosed(): Boolean = closed
        override fun close() {
            closed = true
        }
        override fun isWrapperFor(iface: Class<*>): Boolean = iface.isInstance(this)
        override fun <T : Any?> unwrap(iface: Class<T>): T = iface.cast(this)
    }

    private companion object {
        fun unsupported(): Connection =
            java.lang.reflect.Proxy.newProxyInstance(
                Connection::class.java.classLoader,
                arrayOf(Connection::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "toString" -> "fake"
                    "hashCode" -> 1
                    "equals" -> false
                    else -> throw UnsupportedOperationException(method.name)
                }
            } as Connection
    }
}
