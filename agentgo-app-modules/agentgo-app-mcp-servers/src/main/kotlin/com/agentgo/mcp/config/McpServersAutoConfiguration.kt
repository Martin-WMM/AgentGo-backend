package com.agentgo.mcp.config

import com.agentgo.commons.dto.http.response.status.error.ExceptionResponseCodeRegistry
import com.agentgo.mcp.client.McpStreamableHttpClientImpl
import com.agentgo.mcp.controller.McpServerController
import com.agentgo.mcp.controller.McpServerExceptionHandler
import com.agentgo.mcp.exception.DuplicateMcpServerNameException
import com.agentgo.mcp.exception.InvalidMcpServerRequestException
import com.agentgo.mcp.exception.McpProbeFailedException
import com.agentgo.mcp.exception.McpServerNotFoundException
import com.agentgo.mcp.mapper.McpServerMapperImpl
import com.agentgo.mcp.repository.McpServerRepository
import com.agentgo.mcp.security.AuthenticatedUserIdResolver
import com.agentgo.mcp.service.impl.McpServerServiceImpl
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Auto-configuration entry point for the user-owned MCP Streamable HTTP server module.
 */
@AutoConfiguration
@EntityScan("com.agentgo.mcp.entity")
@EnableJpaRepositories(basePackageClasses = [McpServerRepository::class])
@Import(
    McpServerController::class,
    McpServerExceptionHandler::class,
    McpServerMapperImpl::class,
    McpServerServiceImpl::class,
    McpStreamableHttpClientImpl::class,
    AuthenticatedUserIdResolver::class,
)
class McpServersAutoConfiguration {
    init {
        ExceptionResponseCodeRegistry.register<McpServerNotFoundException>(9501, "MCP-001")
        ExceptionResponseCodeRegistry.register<InvalidMcpServerRequestException>(9502, "MCP-002")
        ExceptionResponseCodeRegistry.register<DuplicateMcpServerNameException>(9503, "MCP-003")
        ExceptionResponseCodeRegistry.register<McpProbeFailedException>(9506, "MCP-006")
    }
}
