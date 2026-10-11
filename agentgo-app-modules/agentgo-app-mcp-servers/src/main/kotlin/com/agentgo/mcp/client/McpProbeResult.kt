package com.agentgo.mcp.client

/**
 * Successful Streamable HTTP probe outcome used to refresh discovery fields.
 *
 * @property protocolVersion negotiated MCP protocol version
 * @property remoteServerName serverInfo.name
 * @property remoteServerVersion serverInfo.version
 * @property serverCapabilities capabilities object from initialize
 * @property tools tools/list entries as JSON-friendly maps
 * @property sessionId optional Mcp-Session-Id returned by the server
 */
data class McpProbeResult(
    val protocolVersion: String?,
    val remoteServerName: String?,
    val remoteServerVersion: String?,
    val serverCapabilities: Map<String, Any?>,
    val tools: List<Map<String, Any?>>,
    val sessionId: String? = null,
)
