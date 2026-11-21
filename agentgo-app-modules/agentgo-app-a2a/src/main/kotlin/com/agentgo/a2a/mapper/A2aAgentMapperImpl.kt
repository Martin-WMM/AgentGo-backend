package com.agentgo.a2a.mapper

import com.agentgo.a2a.entity.A2aAgentEntity
import com.agentgo.a2a.service.NormalizedA2aAgent
import com.agentgo.dto.a2a.A2aAgentResponse
import org.springframework.stereotype.Component

/** Default [A2aAgentMapper] implementation used by the A2A agent service. */
@Component
class A2aAgentMapperImpl : A2aAgentMapper {
    override fun toResponse(entity: A2aAgentEntity): A2aAgentResponse =
        A2aAgentResponse(
            id = requireNotNull(entity.id) { "Persisted A2A agent must have an identifier." },
            cardUrl = entity.cardUrl,
            groupName = entity.groupName,
            name = entity.name,
            description = entity.description,
            tags = entity.tags.toList(),
            metadata = entity.metadata.toMap(),
            descriptionVector = descriptionVector(entity.descriptionVector),
            enabled = entity.enabled,
            status = entity.status,
            createdAt = requireNotNull(entity.createdAt) {
                "Persisted A2A agent must have a creation timestamp."
            },
            updatedAt = requireNotNull(entity.updatedAt) {
                "Persisted A2A agent must have an update timestamp."
            },
        )

    override fun toEntity(ownerUserId: String, agent: NormalizedA2aAgent): A2aAgentEntity =
        A2aAgentEntity(
            ownerUserId = ownerUserId,
            cardUrl = agent.cardUrl,
            groupName = agent.groupName,
            name = agent.name,
            description = agent.description,
            tags = agent.tags.toMutableList(),
            metadata = agent.metadata.toMutableMap(),
            enabled = agent.enabled,
            status = agent.status,
        )

    override fun applyUpdate(entity: A2aAgentEntity, agent: NormalizedA2aAgent) {
        entity.cardUrl = agent.cardUrl
        entity.groupName = agent.groupName
        entity.name = agent.name
        entity.description = agent.description
        entity.tags = agent.tags.toMutableList()
        entity.metadata = agent.metadata.toMutableMap()
        entity.enabled = agent.enabled
        entity.status = agent.status
    }

    private fun descriptionVector(values: List<*>): List<Double> =
        values.map { value ->
            when (value) {
                is Number -> value.toDouble()
                else -> throw IllegalArgumentException("Description vector contains a non-numeric value.")
            }
        }
}
