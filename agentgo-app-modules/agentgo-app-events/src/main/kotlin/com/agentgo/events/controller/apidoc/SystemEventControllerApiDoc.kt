package com.agentgo.events.controller.apidoc

import com.agentgo.commons.api.ApiPaths
import com.agentgo.commons.dto.http.request.PageRequest
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.PageResponse
import com.agentgo.dto.events.SystemEventCreateRequest
import com.agentgo.dto.events.SystemEventResponse
import com.agentgo.dto.events.SystemEventUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Springdoc contract for the global system-event log under `/api/v1/systemEvents`.
 *
 * Every authenticated user sees the same log. Retention keeps the newest 1,000 events
 * from the last three months and discards the rest.
 */
@Tag(
    name = "System events",
    description = "Global system event log at /api/v1/systemEvents. Events are not owned by " +
        "one user. name and domain are required and at most 255 characters. operator is the " +
        "related person; a blank value stores the authenticated user. comment is optional and " +
        "at most 4000 characters. occurredAt is the event time; create omits it to use the " +
        "current time, and update omits it to keep the stored time. Rows older than three months " +
        "are deleted. After that, only the newest 1000 rows are kept. List is paginated: page " +
        "starts at 1, size defaults to 20 and must be at most 100. sort accepts name, domain, " +
        "operator, occurredAt, and createdAt. When sort is omitted, events are ordered by " +
        "occurredAt descending. Optional domain and operator query parameters filter by exact " +
        "match. Delete returns HTTP 204. Success codes are 9800 through 9804. EVENT-001 is not " +
        "found and EVENT-002 is an invalid request. Missing authentication uses AUTH-002.",
)
@RequestMapping(ApiPaths.V1_SYSTEM_EVENTS)
interface SystemEventControllerApiDoc {
    @Operation(
        summary = "Record a global system event",
        description = "Stores one event for every authenticated user to read. A blank operator " +
            "stores the caller. An omitted occurredAt uses the current time. Returns 9801 " +
            "EVENT-CREATED. EVENT-002 rejects a blank name or domain, an oversized field, an " +
            "event older than three months, or an event that would fall outside the newest 1000.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "System event created (9801 EVENT-CREATED)", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid system event request (EVENT-002)"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
        ],
    )
    @PostMapping
    fun create(@Valid @RequestBody request: SystemEventCreateRequest): CommonHttpResponse<SystemEventResponse>

    @Operation(
        summary = "Get one global system event",
        description = "Returns one event by id. Returns 9802 EVENT-RETRIEVED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "System event retrieved (9802 EVENT-RETRIEVED)", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "404", description = "System event not found (EVENT-001)"),
        ],
    )
    @GetMapping("/{id}")
    fun get(
        @Parameter(
            description = "System event identifier",
            `in` = ParameterIn.PATH,
            required = true,
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        )
        @PathVariable("id") id: Long,
    ): CommonHttpResponse<SystemEventResponse>

    @Operation(
        summary = "List global system events",
        description = "Returns one page of events. page starts at 1. size defaults to 20 and must " +
            "be at most 100. sort accepts name, domain, operator, occurredAt, and createdAt. " +
            "When sort is omitted, events are ordered by occurredAt descending. order is ASC or " +
            "DESC and applies to every requested sort field. domain and operator are optional " +
            "exact-match filters. Returns 9800 EVENT-LIST-RETRIEVED. EVENT-002 rejects an unknown " +
            "sort field or a size above 100.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "System events listed (9800 EVENT-LIST-RETRIEVED)", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid page request (EVENT-002)"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
        ],
    )
    @GetMapping
    fun list(
        @ParameterObject @ModelAttribute request: PageRequest,
        @Parameter(description = "Exact domain filter. Blank is ignored.", example = "models")
        @RequestParam(name = "domain", required = false) domain: String?,
        @Parameter(description = "Exact operator filter. Blank is ignored.", example = "ada")
        @RequestParam(name = "operator", required = false) operatorName: String?,
    ): CommonHttpResponse<PageResponse<SystemEventResponse>>

    @Operation(
        summary = "Replace a global system event",
        description = "Replaces name, domain, operator, and comment. A blank operator stores the " +
            "caller. An omitted occurredAt keeps the stored time. Retention still applies. " +
            "Returns 9803 EVENT-UPDATED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "System event updated (9803 EVENT-UPDATED)", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid system event request (EVENT-002)"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "404", description = "System event not found (EVENT-001)"),
        ],
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "System event identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable("id") id: Long,
        @Valid @RequestBody request: SystemEventUpdateRequest,
    ): CommonHttpResponse<SystemEventResponse>

    @Operation(
        summary = "Delete a global system event",
        description = "Deletes one event and returns HTTP 204 with an empty body. The success " +
            "code 9804 EVENT-DELETED is registered but is not sent with this response.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "System event deleted"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "404", description = "System event not found (EVENT-001)"),
        ],
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "System event identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable("id") id: Long,
    ): ResponseEntity<Void>
}
