package com.agentgo.core.model

/**
 * Turns a streaming ReAct completion into user-visible answer deltas.
 *
 * Tool JSON is suppressed. `{"type":"final","content":"..."}` emits the
 * decoded content field as it grows. Plain prose emits every new character.
 *
 * @param onAnswerDelta callback for each newly visible answer fragment
 */
class StreamingAnswerProbe(
    private val onAnswerDelta: (String) -> Unit,
) {
    private val raw = StringBuilder()
    private var mode: Mode = Mode.UNKNOWN
    private var emittedLength: Int = 0
    private var contentValueStart: Int = -1

    /** Full raw model text accumulated so far. */
    fun snapshot(): String = raw.toString()

    /**
     * Accepts one provider text fragment.
     *
     * @param chunk raw model delta
     */
    fun accept(chunk: String) {
        if (chunk.isEmpty()) {
            return
        }
        raw.append(chunk)
        classify()
        when (mode) {
            Mode.PROSE -> emitDecoded(raw.toString())
            Mode.FINAL -> emitFinalContent()
            Mode.TOOL, Mode.UNKNOWN -> Unit
        }
    }

    private fun classify() {
        if (mode != Mode.UNKNOWN) {
            return
        }
        val trimmedStart = raw.indexOfFirst { !it.isWhitespace() }
        if (trimmedStart < 0) {
            return
        }
        if (raw[trimmedStart] != '{') {
            mode = Mode.PROSE
            return
        }
        val head = raw.substring(trimmedStart)
        when {
            TYPE_TOOL.containsMatchIn(head) -> mode = Mode.TOOL
            TYPE_FINAL.containsMatchIn(head) -> {
                mode = Mode.FINAL
                locateContentValueStart()
            }
            head.length > 64 && !TYPE_KEY.containsMatchIn(head) -> mode = Mode.PROSE
        }
    }

    private fun locateContentValueStart() {
        if (contentValueStart >= 0) {
            return
        }
        val marker = Regex(""""content"\s*:\s*"""")
        val match = marker.find(raw) ?: return
        contentValueStart = match.range.last + 1
    }

    private fun emitFinalContent() {
        locateContentValueStart()
        if (contentValueStart < 0) {
            return
        }
        val decoded = decodeJsonStringPrefix(raw.substring(contentValueStart)) ?: return
        emitDecoded(decoded)
    }

    private fun emitDecoded(fullAnswer: String) {
        if (fullAnswer.length <= emittedLength) {
            return
        }
        val delta = fullAnswer.substring(emittedLength)
        emittedLength = fullAnswer.length
        if (delta.isNotEmpty()) {
            onAnswerDelta(delta)
        }
    }

    /**
     * Decodes a JSON string body that may still be incomplete.
     *
     * @return decoded text so far, or null when the trailing escape is incomplete
     */
    private fun decodeJsonStringPrefix(body: String): String? {
        val out = StringBuilder()
        var index = 0
        while (index < body.length) {
            val ch = body[index]
            when {
                ch == '"' -> return out.toString()
                ch == '\\' -> {
                    if (index + 1 >= body.length) {
                        return out.toString()
                    }
                    when (val escaped = body[index + 1]) {
                        '"', '\\', '/' -> out.append(escaped)
                        'b' -> out.append('\b')
                        'f' -> out.append('\u000c')
                        'n' -> out.append('\n')
                        'r' -> out.append('\r')
                        't' -> out.append('\t')
                        'u' -> {
                            if (index + 5 >= body.length) {
                                return out.toString()
                            }
                            val hex = body.substring(index + 2, index + 6)
                            out.append(hex.toInt(16).toChar())
                            index += 6
                            continue
                        }
                        else -> out.append(escaped)
                    }
                    index += 2
                }
                else -> {
                    out.append(ch)
                    index += 1
                }
            }
        }
        return out.toString()
    }

    private enum class Mode {
        UNKNOWN,
        PROSE,
        TOOL,
        FINAL,
    }

    companion object {
        private val TYPE_KEY: Regex = Regex(""""type"\s*:""", RegexOption.IGNORE_CASE)
        private val TYPE_TOOL: Regex = Regex(""""type"\s*:\s*"tool"""", RegexOption.IGNORE_CASE)
        private val TYPE_FINAL: Regex = Regex(""""type"\s*:\s*"final"""", RegexOption.IGNORE_CASE)
    }
}
