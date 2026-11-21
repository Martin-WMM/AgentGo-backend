package com.agentgo.a2a.service

import com.agentgo.dto.a2a.A2aAgentCreateRequest
import com.agentgo.dto.a2a.A2aAgentResponse
import com.agentgo.dto.a2a.A2aAgentStatus
import com.agentgo.dto.a2a.A2aAgentUpdateRequest

/**
 * Application service for authenticated, user-scoped A2A agent management.
 *
 * Implementations must resolve ownership exclusively from the security context and
 * must never trust a client-supplied user identifier. The description vector is derived
 * from the description and is not accepted from the client.
 */
interface A2aAgentService {
    /**
     * Creates an agent for the authenticated user.
     *
     * @param request create payload
     * @return stored agent, including the derived description vector
     */
    fun create(request: A2aAgentCreateRequest): A2aAgentResponse

    /**
     * Loads one agent owned by the authenticated user.
     *
     * @param id agent identifier
     * @return stored agent
     */
    fun get(id: Long): A2aAgentResponse

    /**
     * Lists agents owned by the authenticated user, newest update first.
     *
     * @param status optional operator-status filter
     * @return matching agents
     */
    fun list(status: A2aAgentStatus? = null): List<A2aAgentResponse>

    /**
     * Replaces the editable fields of an agent owned by the authenticated user.
     *
     * @param id agent identifier
     * @param request replacement payload
     * @return stored agent
     */
    fun update(id: Long, request: A2aAgentUpdateRequest): A2aAgentResponse

    /**
     * Deletes an agent owned by the authenticated user.
     *
     * @param id agent identifier
     */
    fun delete(id: Long)

    /**
     * Deletes every listed agent owned by the authenticated user.
     *
     * The operation is all-or-nothing. A missing id deletes none of the agents.
     *
     * @param ids agent identifiers
     */
    fun deleteAll(ids: List<Long>)
}
