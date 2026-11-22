package com.agentgo.ai.checkpoint

import java.io.PrintWriter
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.sql.Connection
import java.sql.SQLException
import java.sql.SQLFeatureNotSupportedException
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger
import javax.sql.DataSource

/**
 * DataSource adapter that contains the langgraph4j PostgresSaver connection leak.
 *
 * `AbstractPostgresSaver.exec()` borrows a connection and never closes it. This
 * adapter reuses one open connection per calling thread and tracks every borrowed
 * connection so [releaseAll] can return them to the pool after an agent run.
 *
 * @property target shared application datasource
 */
class CheckpointDataSource(
    private val target: DataSource,
) : DataSource {
    private val threadConnection = ThreadLocal<Connection>()
    private val openConnections = ConcurrentHashMap.newKeySet<Connection>()

    override fun getConnection(): Connection {
        threadConnection.get()?.takeUnless { it.isClosed }?.let { return it }
        val raw = target.connection
        val tracked = track(raw)
        threadConnection.set(tracked)
        return tracked
    }

    override fun getConnection(username: String?, password: String?): Connection = connection

    /**
     * Closes every tracked checkpoint connection and clears thread locals.
     *
     * Call after each agent stream so ForkJoin workers that hit the leaky
     * `exec()` path do not permanently exhaust the Hikari pool.
     */
    fun releaseAll() {
        val snapshot = openConnections.toList()
        openConnections.clear()
        snapshot.forEach { connection ->
            runCatching { if (!connection.isClosed) connection.close() }
        }
        threadConnection.remove()
    }

    private fun track(raw: Connection): Connection {
        val handler = ConnectionInvocationHandler(raw)
        val proxy = Proxy.newProxyInstance(
            Connection::class.java.classLoader,
            arrayOf(Connection::class.java),
            handler,
        ) as Connection
        handler.proxy = proxy
        openConnections.add(proxy)
        return proxy
    }

    private inner class ConnectionInvocationHandler(
        private val raw: Connection,
    ) : InvocationHandler {
        lateinit var proxy: Connection

        override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
            if (method.name == "close") {
                openConnections.remove(this.proxy)
                if (threadConnection.get() === this.proxy) {
                    threadConnection.remove()
                }
                raw.close()
                return null
            }
            if (method.name == "isWrapperFor" && args?.size == 1) {
                val type = args[0] as Class<*>
                return type.isInstance(raw) || raw.isWrapperFor(type)
            }
            if (method.name == "unwrap" && args?.size == 1) {
                val type = args[0] as Class<*>
                if (type.isInstance(raw)) {
                    return raw
                }
                return raw.unwrap(type)
            }
            if (method.name == "equals" && args?.size == 1) {
                return this.proxy === args[0]
            }
            if (method.name == "hashCode" && args == null) {
                return System.identityHashCode(this.proxy)
            }
            return try {
                method.invoke(raw, *(args ?: emptyArray()))
            } catch (ex: java.lang.reflect.InvocationTargetException) {
                throw ex.targetException
            }
        }
    }

    override fun getLogWriter(): PrintWriter = target.logWriter

    override fun setLogWriter(out: PrintWriter?) {
        target.logWriter = out
    }

    override fun setLoginTimeout(seconds: Int) {
        target.loginTimeout = seconds
    }

    override fun getLoginTimeout(): Int = target.loginTimeout

    override fun getParentLogger(): Logger =
        try {
            target.parentLogger
        } catch (_: SQLFeatureNotSupportedException) {
            Logger.getLogger("global")
        }

    override fun <T : Any?> unwrap(iface: Class<T>): T {
        if (iface.isInstance(this)) {
            return iface.cast(this)
        }
        return target.unwrap(iface)
    }

    override fun isWrapperFor(iface: Class<*>): Boolean =
        iface.isInstance(this) || target.isWrapperFor(iface)

    override fun createConnectionBuilder() =
        throw SQLException("ConnectionBuilder is not supported for checkpoint datasource")
}
