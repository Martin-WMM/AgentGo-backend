package com.agentgo.a2a.service

import com.agentgo.a2a.exception.InvalidA2aAgentRequestException
import com.agentgo.dto.a2a.A2aAgentCreateRequest
import com.agentgo.dto.a2a.A2aAgentStatus
import com.agentgo.dto.a2a.A2aAgentUpdateRequest
import java.net.URI
import org.springframework.stereotype.Component

/**
 * Validates and canonicalizes A2A agent create and update payloads.
 */
@Component
class A2aAgentRequestNormalizer {
    /**
     * Returns a validated create payload.
     *
     * @param request raw create body
     * @return normalized fields
     * @throws InvalidA2aAgentRequestException when a field violates the A2A agent contract
     */
    fun normalizeCreate(request: A2aAgentCreateRequest): NormalizedA2aAgent =
        normalize(
            cardUrl = request.cardUrl,
            groupName = request.groupName,
            name = request.name,
            description = request.description,
            tags = request.tags,
            metadata = request.metadata,
            enabled = request.enabled,
            status = request.status,
        )

    /**
     * Returns a validated update payload.
     *
     * @param request raw update body
     * @return normalized fields
     * @throws InvalidA2aAgentRequestException when a field violates the A2A agent contract
     */
    fun normalizeUpdate(request: A2aAgentUpdateRequest): NormalizedA2aAgent =
        normalize(
            cardUrl = request.cardUrl,
            groupName = request.groupName,
            name = request.name,
            description = request.description,
            tags = request.tags,
            metadata = request.metadata,
            enabled = request.enabled,
            status = request.status,
        )

    private fun normalize(
        cardUrl: String,
        groupName: String,
        name: String,
        description: String?,
        tags: List<String>?,
        metadata: Map<String, Any?>?,
        enabled: Boolean?,
        status: A2aAgentStatus?,
    ): NormalizedA2aAgent = NormalizedA2aAgent(
        cardUrl = requireCardUrl(cardUrl),
        groupName = requireText(groupName, "groupName", NAME_MAX_LENGTH),
        name = requireText(name, "name", NAME_MAX_LENGTH),
        description = normalizeDescription(description),
        tags = normalizeTags(tags),
        metadata = normalizeMetadata(metadata),
        enabled = enabled ?: true,
        status = status ?: A2aAgentStatus.UNKNOWN,
    )

    private fun requireCardUrl(value: String): String {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            throw InvalidA2aAgentRequestException("The A2A card URL must not be blank.")
        }
        if (trimmed.length > CARD_URL_MAX_LENGTH) {
            throw InvalidA2aAgentRequestException(
                "The A2A card URL must be at most $CARD_URL_MAX_LENGTH characters.",
            )
        }
        val uri = try {
            URI.create(trimmed)
        } catch (_: Exception) {
            throw InvalidA2aAgentRequestException("The A2A card URL must be a valid absolute HTTP(S) URL.")
        }
        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") {
            throw InvalidA2aAgentRequestException("The A2A card URL must use http or https.")
        }
        if (uri.host.isNullOrBlank()) {
            throw InvalidA2aAgentRequestException("The A2A card URL must include a host.")
        }
        return trimmed
    }

    private fun requireText(value: String, fieldName: String, maxLength: Int): String {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            throw InvalidA2aAgentRequestException("The A2A agent $fieldName must not be blank.")
        }
        if (trimmed.length > maxLength) {
            throw InvalidA2aAgentRequestException(
                "The A2A agent $fieldName must be at most $maxLength characters.",
            )
        }
        return trimmed
    }

    private fun normalizeDescription(description: String?): String? {
        val trimmed = description?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return null
        }
        if (trimmed.length > DESCRIPTION_MAX_LENGTH) {
            throw InvalidA2aAgentRequestException(
                "The A2A agent description must be at most $DESCRIPTION_MAX_LENGTH characters.",
            )
        }
        return trimmed
    }

    private fun normalizeTags(tags: List<String>?): List<String> {
        val normalized = tags.orEmpty().map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (normalized.size > MAX_TAGS) {
            throw InvalidA2aAgentRequestException("The A2A agent tags support at most $MAX_TAGS entries.")
        }
        val oversized = normalized.firstOrNull { it.length > TAG_MAX_LENGTH }
        if (oversized != null) {
            throw InvalidA2aAgentRequestException(
                "Each A2A agent tag must be at most $TAG_MAX_LENGTH characters.",
            )
        }
        return normalized
    }

    private fun normalizeMetadata(metadata: Map<String, Any?>?): Map<String, Any?> =
        normalizeMap(metadata ?: emptyMap<String, Any?>(), depth = 1)

    private fun normalizeMap(metadata: Map<*, *>, depth: Int): Map<String, Any?> {
        if (depth > MAX_METADATA_DEPTH) {
            throw InvalidA2aAgentRequestException("A2A agent metadata is nested too deeply.")
        }
        if (metadata.size > MAX_METADATA_ENTRIES) {
            throw InvalidA2aAgentRequestException(
                "The A2A agent metadata supports at most $MAX_METADATA_ENTRIES entries.",
            )
        }
        return metadata.entries.associate { (key, value) ->
            val name = key?.toString()?.trim().orEmpty()
            if (name.isEmpty() || name.length > METADATA_KEY_MAX_LENGTH) {
                throw InvalidA2aAgentRequestException(
                    "A2A agent metadata keys must be 1..$METADATA_KEY_MAX_LENGTH characters.",
                )
            }
            name to normalizeMetadataValue(value, depth)
        }
    }

    private fun normalizeMetadataValue(value: Any?, depth: Int): Any? = when (value) {
        null -> null
        is String -> {
            if (value.length > METADATA_STRING_MAX_LENGTH) {
                throw InvalidA2aAgentRequestException(
                    "A2A agent metadata strings must be at most $METADATA_STRING_MAX_LENGTH characters.",
                )
            }
            value
        }
        is Boolean -> value
        is Number -> {
            if (!value.toDouble().isFinite()) {
                throw InvalidA2aAgentRequestException("A2A agent metadata numbers must be finite.")
            }
            value
        }
        is Map<*, *> -> normalizeMap(value, depth + 1)
        is List<*> -> {
            if (value.size > MAX_METADATA_ENTRIES) {
                throw InvalidA2aAgentRequestException(
                    "A2A agent metadata arrays support at most $MAX_METADATA_ENTRIES entries.",
                )
            }
            value.map { normalizeMetadataValue(it, depth + 1) }
        }
        else -> throw InvalidA2aAgentRequestException(
            "A2A agent metadata values must be JSON scalars, arrays, or objects.",
        )
    }

    private companion object {
        const val NAME_MAX_LENGTH = 255
        const val CARD_URL_MAX_LENGTH = 2048
        const val DESCRIPTION_MAX_LENGTH = 4000
        const val TAG_MAX_LENGTH = 32
        const val MAX_TAGS = 16
        const val MAX_METADATA_ENTRIES = 32
        const val METADATA_KEY_MAX_LENGTH = 64
        const val METADATA_STRING_MAX_LENGTH = 2000
        const val MAX_METADATA_DEPTH = 4
    }
}
