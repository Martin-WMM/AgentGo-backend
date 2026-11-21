package com.agentgo.dto.a2a

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * API representation of a user-owned A2A agent.
 */
@Schema(description = "User-owned A2A agent returned by the a2aAgents API")
data class A2aAgentResponse(
    @field:Schema(
        description = "Persistent A2A agent identifier",
        format = "int64",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val id: Long,
    @field:Schema(
        description = "Absolute HTTP(S) URL of the remote A2A Agent Card",
        example = "https://agent.example.com/.well-known/agent-card.json",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val cardUrl: String,
    @field:Schema(
        description = "Group name. Unique together with name and the creator.",
        example = "billing",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val groupName: String,
    @field:Schema(
        description = "Human-readable A2A agent name, unique with groupName and the creator",
        example = "billing-agent",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val name: String,
    @field:Schema(
        description = "Optional free-form description",
        example = "Handles invoice questions for the billing workspace.",
    )
    val description: String? = null,
    @field:Schema(
        description = "Display tags",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val tags: List<String>,
    @field:Schema(
        description = "Caller-defined JSON object metadata",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val metadata: Map<String, Any?>,
    @field:Schema(
        description = "64-dimension L2-normalized feature hash of the description. " +
            "An empty description is stored as a zero vector.",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val descriptionVector: List<Double>,
    @field:Schema(
        description = "Whether the agent is available for Agent runtime assembly",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val enabled: Boolean,
    @field:Schema(
        description = "Operator status",
        example = "AVAILABLE",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val status: A2aAgentStatus,
    @field:Schema(
        description = "Creation timestamp",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val createdAt: Instant,
    @field:Schema(
        description = "Last modification timestamp",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val updatedAt: Instant,
)
