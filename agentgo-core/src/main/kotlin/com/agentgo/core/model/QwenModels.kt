package com.agentgo.core.model

/**
 * Qwen models AgentGo can run.
 *
 * The catalog matches the Alibaba Cloud Model Studio text-generation list
 * published on 2026-11-16. Snapshots and preview aliases are omitted.
 * [DEFAULT_ID] is the documented starting point: capable function calling
 * at a balanced cost.
 */
object QwenModels {
    const val DEFAULT_ID: String = "qwen3.8-27b"

    /** China (Beijing) OpenAI-compatible DashScope root. */
    const val DASHSCOPE_COMPATIBLE_BASE_URL: String =
        "https://dashscope.aliyuncs.com/compatible-mode/v1"

    /** International OpenAI-compatible DashScope root. */
    const val DASHSCOPE_INTL_COMPATIBLE_BASE_URL: String =
        "https://dashscope-intl.aliyuncs.com/compatible-mode/v1"

    private const val CONTEXT_1M: Int = 1_000_000
    private const val CONTEXT_256K: Int = 262_144
    private const val CONTEXT_128K: Int = 131_072
    private const val CONTEXT_10M: Int = 10_000_000
    private const val CONTEXT_32K: Int = 32_768
    private const val CONTEXT_16K: Int = 16_384
    private const val CONTEXT_8K: Int = 8_192

    private val catalog: List<QwenModel> = listOf(
        model("qwen4-max", "Qwen 4 Max", CONTEXT_1M, functionCalling = true, thinking = true, structured = true, QwenModelTier.FLAGSHIP),
        model("qwen3.8-max", "Qwen 3.8 Max", CONTEXT_1M, functionCalling = true, thinking = true, structured = true, QwenModelTier.FLAGSHIP),
        model("qwen3.8-flash", "Qwen 3.8 Flash", CONTEXT_1M, functionCalling = true, thinking = true, structured = true, QwenModelTier.FAST),
        model("qwen3.8-27b", "Qwen 3.8 27B", CONTEXT_256K, functionCalling = true, thinking = true, structured = true, QwenModelTier.BALANCED),
        model("qwen3.7-max", "Qwen 3.7 Max", CONTEXT_1M, functionCalling = true, thinking = true, structured = true, QwenModelTier.FLAGSHIP),
        model("qwen3.7-plus", "Qwen 3.7 Plus", CONTEXT_1M, functionCalling = true, thinking = true, structured = true, QwenModelTier.BALANCED),
        model("qwen3.7-flash", "Qwen 3.7 Flash", CONTEXT_1M, functionCalling = true, thinking = true, structured = true, QwenModelTier.FAST),
        model("qwen3.6-plus", "Qwen 3.6 Plus", CONTEXT_1M, functionCalling = true, thinking = true, structured = true, QwenModelTier.BALANCED),
        model("qwen3.6-flash", "Qwen 3.6 Flash", CONTEXT_1M, functionCalling = true, thinking = true, structured = true, QwenModelTier.FAST),
        model("qwen3.5-plus", "Qwen 3.5 Plus", CONTEXT_1M, functionCalling = true, thinking = true, structured = true, QwenModelTier.BALANCED),
        model("qwen3.5-flash", "Qwen 3.5 Flash", CONTEXT_1M, functionCalling = true, thinking = true, structured = true, QwenModelTier.FAST),
        model("qwen3.5-397b-a17b", "Qwen 3.5 397B A17B", CONTEXT_256K, functionCalling = true, thinking = true, structured = true, QwenModelTier.FLAGSHIP),
        model("qwen3.5-122b-a10b", "Qwen 3.5 122B A10B", CONTEXT_256K, functionCalling = true, thinking = true, structured = true, QwenModelTier.BALANCED),
        model("qwen3.5-27b", "Qwen 3.5 27B", CONTEXT_256K, functionCalling = true, thinking = true, structured = true, QwenModelTier.FAST),
        model("qwen3.5-35b-a3b", "Qwen 3.5 35B A3B", CONTEXT_256K, functionCalling = true, thinking = true, structured = true, QwenModelTier.FAST),
        model("qwen3-coder-flash", "Qwen 3 Coder Flash", CONTEXT_1M, functionCalling = true, thinking = true, structured = true, QwenModelTier.CODER),
        model("qwen-plus", "Qwen Plus", CONTEXT_1M, functionCalling = true, thinking = true, structured = true, QwenModelTier.GENERAL),
        model("qwen-max", "Qwen Max", CONTEXT_128K, functionCalling = true, thinking = false, structured = true, QwenModelTier.GENERAL),
        model("qwen-flash", "Qwen Flash", CONTEXT_1M, functionCalling = true, thinking = true, structured = true, QwenModelTier.FAST),
        model("qwen-long", "Qwen Long", CONTEXT_10M, functionCalling = false, thinking = false, structured = true, QwenModelTier.SPECIALIZED),
        model("qwen-mt-plus", "Qwen MT Plus", CONTEXT_16K, functionCalling = false, thinking = false, structured = false, QwenModelTier.SPECIALIZED),
        model("qwen-mt-flash", "Qwen MT Flash", CONTEXT_16K, functionCalling = false, thinking = false, structured = false, QwenModelTier.SPECIALIZED),
        model("qwen-mt-lite", "Qwen MT Lite", CONTEXT_16K, functionCalling = false, thinking = false, structured = false, QwenModelTier.SPECIALIZED),
        model("qwen-plus-character", "Qwen Plus Character", CONTEXT_32K, functionCalling = false, thinking = false, structured = false, QwenModelTier.SPECIALIZED),
        model("qwen-flash-character", "Qwen Flash Character", CONTEXT_8K, functionCalling = false, thinking = false, structured = false, QwenModelTier.SPECIALIZED),
    )

    private val byId: Map<String, QwenModel> = catalog.associateBy { it.id }

    /** Default model for a new AgentGo agent. */
    val default: QwenModel
        get() = require(DEFAULT_ID)

    /** Every catalog entry, in selection order. */
    fun all(): List<QwenModel> = catalog

    /**
     * Models that can drive the tool loop.
     *
     * @return models with function calling enabled
     */
    fun agentCapable(): List<QwenModel> = catalog.filter { it.supportsFunctionCalling }

    /**
     * Finds a catalog entry.
     *
     * @param id DashScope model id
     * @return the model, or null when the id is absent
     */
    fun find(id: String): QwenModel? = byId[id]

    /**
     * Returns a catalog entry.
     *
     * @param id DashScope model id
     * @return the model
     * @throws IllegalArgumentException when the id is absent
     */
    fun require(id: String): QwenModel =
        find(id) ?: throw IllegalArgumentException("Unknown Qwen model: $id")

    /**
     * Returns a model that can request tools.
     *
     * @param id DashScope model id
     * @return the model
     * @throws IllegalArgumentException when the id is unknown or cannot call tools
     */
    fun requireAgentCapable(id: String): QwenModel {
        val model = require(id)
        if (!model.supportsFunctionCalling) {
            throw IllegalArgumentException("Qwen model $id does not support function calling.")
        }
        return model
    }

    private fun model(
        id: String,
        displayName: String,
        contextTokens: Int,
        functionCalling: Boolean,
        thinking: Boolean,
        structured: Boolean,
        tier: QwenModelTier,
    ): QwenModel = QwenModel(
        id = id,
        displayName = displayName,
        contextTokens = contextTokens,
        supportsFunctionCalling = functionCalling,
        supportsThinking = thinking,
        supportsStructuredOutput = structured,
        tier = tier,
    )
}
