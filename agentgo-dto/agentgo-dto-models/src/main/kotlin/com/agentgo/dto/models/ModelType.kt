package com.agentgo.dto.models

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Supported categories of user-managed AI models.
 *
 * Values are persisted and exposed over the API as uppercase identifiers.
 */
@Schema(
    description = "Category of a user-managed AI model",
    allowableValues = ["LLM", "MULTIMODAL", "EMBEDDING", "ASR", "TTS", "SPEECH2SPEECH", "OTHER"],
)
enum class ModelType {
    /** Text-oriented large language model. */
    LLM,

    /** Model that accepts or produces multiple modalities such as text and images. */
    MULTIMODAL,

    /** Vector embedding model used for similarity and retrieval workloads. */
    EMBEDDING,

    /** Automatic speech recognition (speech-to-text) model. */
    ASR,

    /** Text-to-speech synthesis model. */
    TTS,

    /** End-to-end speech-to-speech model. */
    SPEECH2SPEECH,

    /** Extensibility bucket for additional model categories. */
    OTHER,
}
