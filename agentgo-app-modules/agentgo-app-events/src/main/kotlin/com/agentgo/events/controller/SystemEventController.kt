package com.agentgo.events.controller

import com.agentgo.commons.annotation.AgentGoLogApi
import com.agentgo.commons.dto.http.request.PageRequest
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.PageResponse
import com.agentgo.commons.dto.http.response.status.success.EventsResponseCodeRegistry
import com.agentgo.dto.events.SystemEventCreateRequest
import com.agentgo.dto.events.SystemEventResponse
import com.agentgo.dto.events.SystemEventUpdateRequest
import com.agentgo.events.controller.apidoc.SystemEventControllerApiDoc
import com.agentgo.events.service.SystemEventService
import com.agentgo.starter.i18n.AgentGoMessages
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * HTTP adapter for the global system-event log.
 *
 * @property systemEventService global event application service
 * @property messages localized API message resolver
 */
@RestController
class SystemEventController(
    private val systemEventService: SystemEventService,
    private val messages: AgentGoMessages,
) : SystemEventControllerApiDoc {
    @AgentGoLogApi(name = "system-event.create")
    override fun create(
        @Valid request: SystemEventCreateRequest,
    ): CommonHttpResponse<SystemEventResponse> =
        CommonHttpResponse(
            code = EventsResponseCodeRegistry.CREATED,
            responseType = EventsResponseCodeRegistry.CREATED_TYPE,
            message = messages.get("event.created", defaultMessage = "System event created"),
            data = systemEventService.create(request),
        )

    @AgentGoLogApi(name = "system-event.get")
    override fun get(id: Long): CommonHttpResponse<SystemEventResponse> =
        CommonHttpResponse(
            code = EventsResponseCodeRegistry.RETRIEVED,
            responseType = EventsResponseCodeRegistry.RETRIEVED_TYPE,
            message = messages.get("event.retrieved", defaultMessage = "System event retrieved"),
            data = systemEventService.get(id),
        )

    @AgentGoLogApi(name = "system-event.list")
    override fun list(
        request: PageRequest,
        domain: String?,
        operatorName: String?,
    ): CommonHttpResponse<PageResponse<SystemEventResponse>> =
        CommonHttpResponse(
            code = EventsResponseCodeRegistry.LIST_RETRIEVED,
            responseType = EventsResponseCodeRegistry.LIST_RETRIEVED_TYPE,
            message = messages.get("event.listed", defaultMessage = "System events listed"),
            data = systemEventService.list(request, domain, operatorName),
        )

    @AgentGoLogApi(name = "system-event.update")
    override fun update(
        id: Long,
        @Valid request: SystemEventUpdateRequest,
    ): CommonHttpResponse<SystemEventResponse> =
        CommonHttpResponse(
            code = EventsResponseCodeRegistry.UPDATED,
            responseType = EventsResponseCodeRegistry.UPDATED_TYPE,
            message = messages.get("event.updated", defaultMessage = "System event updated"),
            data = systemEventService.update(id, request),
        )

    @AgentGoLogApi(name = "system-event.delete")
    override fun delete(id: Long): ResponseEntity<Void> {
        systemEventService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
