package com.agentgo.prompts.revision

/**
 * Minimal unified diff for prompt metadata and template bodies.
 *
 * Identical inputs produce null. A changed region keeps three lines of context.
 * When the changed region is too large for a line-level edit script, the region
 * is emitted as one deletion followed by one insertion.
 */
object UnifiedDiff {
    private const val CONTEXT = 3
    private const val MAX_CELLS = 1_000_000

    /**
     * Builds a git-style unified diff for one file.
     *
     * @param before text before the commit; empty when the file is new
     * @param after text after the commit; empty when the file is deleted
     * @param path file name used in the diff headers
     * @return unified diff, or null when [before] and [after] are equal
     */
    fun diff(before: String, after: String, path: String): String? {
        if (before == after) return null
        val oldLines = splitLines(before)
        val newLines = splitLines(after)
        val edits = editScript(oldLines, newLines)
        if (edits.all { it is Edit.Equal }) return null
        return format(path, before.isEmpty(), after.isEmpty(), oldLines, newLines, edits)
    }

    private fun splitLines(text: String): List<String> =
        if (text.isEmpty()) emptyList() else text.split('\n')

    private fun editScript(oldLines: List<String>, newLines: List<String>): List<Edit> {
        var prefix = 0
        while (prefix < oldLines.size && prefix < newLines.size && oldLines[prefix] == newLines[prefix]) {
            prefix++
        }
        var suffix = 0
        while (
            suffix < oldLines.size - prefix &&
            suffix < newLines.size - prefix &&
            oldLines[oldLines.size - 1 - suffix] == newLines[newLines.size - 1 - suffix]
        ) {
            suffix++
        }
        val middleOld = oldLines.subList(prefix, oldLines.size - suffix)
        val middleNew = newLines.subList(prefix, newLines.size - suffix)
        val middle = if (middleOld.size.toLong() * middleNew.size.toLong() > MAX_CELLS) {
            listOf(Edit.Delete(middleOld.size), Edit.Insert(middleNew.size))
        } else {
            lineEdits(middleOld, middleNew)
        }
        return buildList {
            if (prefix > 0) add(Edit.Equal(prefix))
            addAll(middle)
            if (suffix > 0) add(Edit.Equal(suffix))
        }
    }

    private fun lineEdits(oldLines: List<String>, newLines: List<String>): List<Edit> {
        val oldSize = oldLines.size
        val newSize = newLines.size
        val lengths = Array(oldSize + 1) { IntArray(newSize + 1) }
        for (oldIndex in oldSize - 1 downTo 0) {
            for (newIndex in newSize - 1 downTo 0) {
                lengths[oldIndex][newIndex] = if (oldLines[oldIndex] == newLines[newIndex]) {
                    lengths[oldIndex + 1][newIndex + 1] + 1
                } else {
                    maxOf(lengths[oldIndex + 1][newIndex], lengths[oldIndex][newIndex + 1])
                }
            }
        }
        val edits = mutableListOf<Edit>()
        var oldIndex = 0
        var newIndex = 0
        while (oldIndex < oldSize && newIndex < newSize) {
            when {
                oldLines[oldIndex] == newLines[newIndex] -> {
                    push(edits, Edit.Equal(1))
                    oldIndex++
                    newIndex++
                }
                lengths[oldIndex + 1][newIndex] >= lengths[oldIndex][newIndex + 1] -> {
                    push(edits, Edit.Delete(1))
                    oldIndex++
                }
                else -> {
                    push(edits, Edit.Insert(1))
                    newIndex++
                }
            }
        }
        if (oldIndex < oldSize) push(edits, Edit.Delete(oldSize - oldIndex))
        if (newIndex < newSize) push(edits, Edit.Insert(newSize - newIndex))
        return edits
    }

    private fun push(edits: MutableList<Edit>, edit: Edit) {
        val previous = edits.lastOrNull()
        when {
            previous is Edit.Equal && edit is Edit.Equal -> edits[edits.lastIndex] = Edit.Equal(previous.count + edit.count)
            previous is Edit.Delete && edit is Edit.Delete -> edits[edits.lastIndex] = Edit.Delete(previous.count + edit.count)
            previous is Edit.Insert && edit is Edit.Insert -> edits[edits.lastIndex] = Edit.Insert(previous.count + edit.count)
            edit.count > 0 -> edits += edit
        }
    }

    private fun format(
        path: String,
        oldFileMissing: Boolean,
        newFileMissing: Boolean,
        oldLines: List<String>,
        newLines: List<String>,
        edits: List<Edit>,
    ): String {
        val ops = expand(oldLines, newLines, edits)
        return buildString {
            appendLine("diff --git a/$path b/$path")
            if (oldFileMissing) appendLine("new file mode 100644")
            if (newFileMissing) appendLine("deleted file mode 100644")
            appendLine("--- ${if (oldFileMissing) "/dev/null" else "a/$path"}")
            appendLine("+++ ${if (newFileMissing) "/dev/null" else "b/$path"}")
            append(hunks(ops))
        }.trimEnd()
    }

    private fun expand(
        oldLines: List<String>,
        newLines: List<String>,
        edits: List<Edit>,
    ): List<LineOp> {
        val ops = mutableListOf<LineOp>()
        var oldIndex = 0
        var newIndex = 0
        for (edit in edits) {
            repeat(edit.count) {
                when (edit) {
                    is Edit.Equal -> {
                        ops += LineOp(' ', oldLines[oldIndex], oldIndex + 1, newIndex + 1)
                        oldIndex++
                        newIndex++
                    }
                    is Edit.Delete -> {
                        ops += LineOp('-', oldLines[oldIndex], oldIndex + 1, null)
                        oldIndex++
                    }
                    is Edit.Insert -> {
                        ops += LineOp('+', newLines[newIndex], null, newIndex + 1)
                        newIndex++
                    }
                }
            }
        }
        return ops
    }

    private fun hunks(ops: List<LineOp>): String {
        val ranges = mutableListOf<IntRange>()
        ops.forEachIndexed { index, op ->
            if (op.kind == ' ') return@forEachIndexed
            val start = maxOf(0, index - CONTEXT)
            val end = minOf(ops.lastIndex, index + CONTEXT)
            val previous = ranges.lastOrNull()
            if (previous != null && start <= previous.last + 1) {
                ranges[ranges.lastIndex] = previous.first..end
            } else {
                ranges += start..end
            }
        }
        return ranges.joinToString("\n") { range ->
            val slice = ops.subList(range.first, range.last + 1)
            val oldCount = slice.count { it.kind != '+' }
            val newCount = slice.count { it.kind != '-' }
            val oldStart = if (oldCount == 0) 0 else requireNotNull(slice.first { it.oldLine != null }.oldLine)
            val newStart = if (newCount == 0) 0 else requireNotNull(slice.first { it.newLine != null }.newLine)
            buildString {
                appendLine("@@ -$oldStart,$oldCount +$newStart,$newCount @@")
                slice.forEach { op -> appendLine("${op.kind}${op.text}") }
            }.trimEnd()
        }
    }

    private data class LineOp(
        val kind: Char,
        val text: String,
        val oldLine: Int?,
        val newLine: Int?,
    )

    private sealed interface Edit {
        val count: Int

        data class Equal(override val count: Int) : Edit
        data class Delete(override val count: Int) : Edit
        data class Insert(override val count: Int) : Edit
    }
}
