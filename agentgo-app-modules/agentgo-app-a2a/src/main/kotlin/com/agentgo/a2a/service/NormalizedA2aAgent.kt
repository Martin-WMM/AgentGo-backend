package com.agentgo.a2a.service

import com.agentgo.dto.a2a.A2aAgentStatus

/**
 * Validated A2A agent fields ready to persist.
 *
 * @property cardUrl absolute HTTP(S) Agent Card URL
 * @property groupName group that scopes [name] for the creator
 * @property name display name unique with [groupName] and the creator
 * @property description trimmed description, or null when blank
 * @property tags distinct display tags
 * @property metadata JSON object metadata
 * @property enabled whether runtime assembly may use the agent
 * @property status operator status requested by the caller
 */
data class NormalizedA2aAgent(
    val cardUrl: String,
    val groupName: String,
    val name: String,
    val description: String?,
    val tags: List<String>,
    val metadata: Map<String, Any?>,
    val enabled: Boolean,
    val status: A2aAgentStatus,
)
