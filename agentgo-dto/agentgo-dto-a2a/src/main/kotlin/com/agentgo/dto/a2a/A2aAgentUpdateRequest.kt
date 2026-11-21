package com.agentgo.dto.a2a

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Request body used to replace a user-owned A2A agent.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 * The description vector is not accepted from the client; the server derives it.
 */
@Schema(description = "Request body for updating a user-owned A2A agent")
class A2aAgentUpdateRequest {
    @field:Schema(
        description = "Absolute HTTP(S) URL of the remote A2A Agent Card",
        example = "https://agent.example.com/.well-known/agent-card.json",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 2048,
    )
    var cardUrl: String = ""

    @field:Schema(
        description = "Group name. Unique together with name and the authenticated creator.",
        example = "billing",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 255,
    )
    var groupName: String = ""

    @field:Schema(
        description = "Human-readable A2A agent name, unique with groupName and the creator",
        example = "billing-agent",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 255,
    )
    var name: String = ""

    @field:Schema(
        description = "Optional description. The server recomputes the description vector from this text.",
        example = "Handles invoice questions for the billing workspace.",
        maxLength = 4000,
    )
    var description: String? = null

    @field:Schema(
        description = "Display tags. Blank entries are removed and duplicates are collapsed.",
        example = "[\"billing\",\"finance\"]",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    var tags: List<String> = emptyList()

    @field:Schema(
        description = "Caller-defined JSON object metadata",
        example = "{\"team\":\"ops\"}",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    var metadata: Map<String, Any?> = emptyMap()

    @field:Schema(
        description = "Whether the agent is available for Agent runtime assembly",
        example = "true",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    var enabled: Boolean? = true

    @field:Schema(
        description = "Operator status. Disabling an AVAILABLE agent stores UNAVAILABLE.",
        example = "AVAILABLE",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    var status: A2aAgentStatus? = A2aAgentStatus.UNKNOWN

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is A2aAgentUpdateRequest) return false
        return fields() == other.fields()
    }

    override fun hashCode(): Int = fields().hashCode()

    private fun fields(): List<Any?> =
        listOf(cardUrl, groupName, name, description, tags, metadata, enabled, status)
}
