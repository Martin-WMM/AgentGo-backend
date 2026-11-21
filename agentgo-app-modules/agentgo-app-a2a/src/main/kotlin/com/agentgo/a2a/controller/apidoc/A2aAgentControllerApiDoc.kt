package com.agentgo.a2a.controller.apidoc

import com.agentgo.commons.api.ApiPaths
import com.agentgo.commons.dto.http.request.BatchIdsRequest
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.dto.a2a.A2aAgentCreateRequest
import com.agentgo.dto.a2a.A2aAgentResponse
import com.agentgo.dto.a2a.A2aAgentStatus
import com.agentgo.dto.a2a.A2aAgentUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Springdoc contract for authenticated A2A agent management endpoints.
 *
 * Every agent belongs to the authenticated user. Clients send an Agent Card URL,
 * name, description, tags, metadata, enabled flag, and status. The description
 * vector is derived on the server.
 */
@Tag(
    name = "A2A Agents",
    description = "Versioned user-owned A2A agents at /api/v1/a2aAgents. " +
        "Every agent belongs to the authenticated user; clients must not send a user id. " +
        "groupName, name, and creator are unique together (HTTP 409, A2A-003). " +
        "Identifiers are bigint values. cardUrl is an absolute HTTP(S) Agent Card URL. " +
        "descriptionVector is a 64-dimension feature hash of description and is not client-supplied. " +
        "Success codes are 9900 through 9904. A2A-001 is not found and A2A-002 is invalid. " +
        "Missing authentication uses AUTH-002.",
)
@RequestMapping(ApiPaths.V1_A2A_AGENTS)
interface A2aAgentControllerApiDoc {
    @Operation(
        summary = "Create an A2A agent for the authenticated user",
        description = "Stores an A2A agent owned by the caller and derives the description vector. " +
            "Returns 9901 A2A-CREATED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "A2A agent created", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid A2A agent request (A2A-002)"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "409", description = "Duplicate A2A agent name (A2A-003)"),
        ],
    )
    @PostMapping
    fun create(@RequestBody request: A2aAgentCreateRequest): CommonHttpResponse<A2aAgentResponse>

    @Operation(
        summary = "Get one A2A agent owned by the authenticated user",
        description = "Returns 9902 A2A-RETRIEVED when the id belongs to the caller.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "A2A agent retrieved", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "404", description = "A2A agent not found (A2A-001)"),
        ],
    )
    @GetMapping("/{id}")
    fun get(
        @Parameter(description = "A2A agent identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable("id") id: Long,
    ): CommonHttpResponse<A2aAgentResponse>

    @Operation(
        summary = "List A2A agents owned by the authenticated user",
        description = "Lists the caller's A2A agents, newest update first. Optional status filter. " +
            "Returns 9900 A2A-LIST-RETRIEVED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "A2A agents listed", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "Authentication required"),
        ],
    )
    @GetMapping
    fun list(
        @Parameter(
            description = "Optional operator status filter",
            `in` = ParameterIn.QUERY,
            example = "AVAILABLE",
        )
        @RequestParam(value = "status", required = false) status: A2aAgentStatus?,
    ): CommonHttpResponse<List<A2aAgentResponse>>

    @Operation(
        summary = "Replace an A2A agent owned by the authenticated user",
        description = "Replaces editable fields and recomputes the description vector. " +
            "Returns 9903 A2A-UPDATED. Disabling an AVAILABLE agent stores UNAVAILABLE.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "A2A agent updated", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid A2A agent request"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "404", description = "A2A agent not found"),
            ApiResponse(responseCode = "409", description = "Duplicate A2A agent name"),
        ],
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "A2A agent identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable("id") id: Long,
        @RequestBody request: A2aAgentUpdateRequest,
    ): CommonHttpResponse<A2aAgentResponse>

    @Operation(
        summary = "Delete an A2A agent owned by the authenticated user",
        description = "Deletes the caller's A2A agent and returns HTTP 204.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "A2A agent deleted"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "404", description = "A2A agent not found (A2A-001)"),
        ],
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "A2A agent identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable("id") id: Long,
    ): ResponseEntity<Void>

    @Operation(
        summary = "Delete A2A agents owned by the authenticated user",
        description = "Deletes every listed A2A agent in one transaction and returns HTTP 204. " +
            "An empty list, more than 100 ids, a non-positive id, or any missing id rejects the " +
            "request and deletes nothing. Another user's id is not found.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "A2A agents deleted"),
            ApiResponse(responseCode = "400", description = "Invalid id list (A2A-002)"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "404", description = "A2A agent not found (A2A-001)"),
        ],
    )
    @PostMapping("/batch-delete")
    fun deleteAll(@RequestBody request: BatchIdsRequest): ResponseEntity<Void>
}
