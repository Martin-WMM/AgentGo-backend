package com.agentgo.a2a.controller

import com.agentgo.a2a.controller.apidoc.A2aAgentControllerApiDoc
import com.agentgo.a2a.service.A2aAgentService
import com.agentgo.commons.dto.http.request.BatchIdsRequest
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.status.success.A2aResponseCodeRegistry
import com.agentgo.dto.a2a.A2aAgentCreateRequest
import com.agentgo.dto.a2a.A2aAgentResponse
import com.agentgo.dto.a2a.A2aAgentStatus
import com.agentgo.dto.a2a.A2aAgentUpdateRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * HTTP adapter for authenticated A2A agent CRUD operations.
 */
@RestController
class A2aAgentController(
    private val a2aAgentService: A2aAgentService,
) : A2aAgentControllerApiDoc {
    override fun create(request: A2aAgentCreateRequest): CommonHttpResponse<A2aAgentResponse> =
        CommonHttpResponse(
            code = A2aResponseCodeRegistry.CREATED,
            responseType = A2aResponseCodeRegistry.CREATED_TYPE,
            message = "A2A agent created",
            data = a2aAgentService.create(request),
        )

    override fun get(id: Long): CommonHttpResponse<A2aAgentResponse> =
        CommonHttpResponse(
            code = A2aResponseCodeRegistry.RETRIEVED,
            responseType = A2aResponseCodeRegistry.RETRIEVED_TYPE,
            message = "A2A agent retrieved",
            data = a2aAgentService.get(id),
        )

    override fun list(status: A2aAgentStatus?): CommonHttpResponse<List<A2aAgentResponse>> =
        CommonHttpResponse(
            code = A2aResponseCodeRegistry.LIST_RETRIEVED,
            responseType = A2aResponseCodeRegistry.LIST_RETRIEVED_TYPE,
            message = "A2A agents listed",
            data = a2aAgentService.list(status),
        )

    override fun update(id: Long, request: A2aAgentUpdateRequest): CommonHttpResponse<A2aAgentResponse> =
        CommonHttpResponse(
            code = A2aResponseCodeRegistry.UPDATED,
            responseType = A2aResponseCodeRegistry.UPDATED_TYPE,
            message = "A2A agent updated",
            data = a2aAgentService.update(id, request),
        )

    override fun delete(id: Long): ResponseEntity<Void> {
        a2aAgentService.delete(id)
        return ResponseEntity.noContent().build()
    }

    override fun deleteAll(request: BatchIdsRequest): ResponseEntity<Void> {
        a2aAgentService.deleteAll(request.ids.orEmpty())
        return ResponseEntity.noContent().build()
    }
}
