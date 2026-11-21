package com.agentgo.prompts.revision

import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.dto.prompts.PromptTemplateMetadata
import com.agentgo.dto.presentation.IconSource
import com.agentgo.dto.presentation.ResourceIcon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PromptTemplateCommitDiffTest {
    @Test
    fun `identical text has no diff`() {
        assertNull(UnifiedDiff.diff("Hello", "Hello", "content"))
    }

    @Test
    fun `formats a one line replacement and a new file`() {
        assertEquals(
            """
            diff --git a/content b/content
            --- a/content
            +++ b/content
            @@ -1,3 +1,3 @@
             alpha
            -beta
            +BETA
             gamma
            """.trimIndent(),
            UnifiedDiff.diff("alpha\nbeta\ngamma", "alpha\nBETA\ngamma", "content"),
        )
        assertEquals(
            """
            diff --git a/content b/content
            new file mode 100644
            --- /dev/null
            +++ b/content
            @@ -0,0 +1,1 @@
            +Hello
            """.trimIndent(),
            UnifiedDiff.diff("", "Hello", "content"),
        )
    }

    @Test
    fun `initial commit records metadata and content additions`() {
        val after = PromptSnapshot(
            groupName = "chat",
            name = "Greeting",
            engine = PromptEngine.SIMPLE,
            content = "Hello",
            description = null,
            metadata = PromptTemplateMetadata().apply {
                color = "#1D4ED8"
                icon = ResourceIcon().apply {
                    source = IconSource.ICONIFY
                    value = "lucide:bot"
                }
            },
            tags = listOf("draft"),
        )

        val diff = PromptTemplateCommitDiff.render(null, after)

        assertTrue(diff.contains("diff --git a/metadata b/metadata"))
        assertTrue(diff.contains("+groupName: chat"))
        assertTrue(diff.contains("+icon.source: ICONIFY"))
        assertTrue(diff.contains("+tags: draft"))
        assertTrue(diff.contains("diff --git a/content b/content"))
        assertTrue(diff.contains("+Hello"))
        assertTrue(after.sameAs(after))
    }
}
