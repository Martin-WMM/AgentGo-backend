package com.agentgo.dto.prompts

import jakarta.validation.ConstraintValidatorContext
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PromptTagsValidatorTest {
    private val validator = PromptTagsValidator()

    @Test
    fun `accepts a missing list blank entries and duplicate tags`() {
        validator.initialize(annotation("tags"))

        assertTrue(validator.isValid(null, unusedContext()))
        assertTrue(validator.isValid(emptyList(), unusedContext()))
        assertTrue(validator.isValid(listOf("  ", "system", "system"), unusedContext()))
    }

    @Test
    fun `rejects a tag that is too long contains a comma or a control character`() {
        validator.initialize(annotation("tags"))
        val context = unusedContext()

        assertFalse(validator.isValid(listOf("x".repeat(33)), context))
        assertFalse(validator.isValid(listOf("a,b"), context))
        assertFalse(validator.isValid(listOf("bad\ntag"), context))
    }

    @Test
    fun `enforces the configured tag count and length`() {
        val limits = annotation("tiny")
        validator.initialize(limits)
        val context = unusedContext()

        assertEquals(2, limits.maxCount)
        assertEquals(4, limits.maxLength)
        assertEquals("too many", limits.message)
        assertEquals(0, limits.groups.size)
        assertEquals(0, limits.payload.size)
        assertTrue(validator.isValid(listOf("abcd", "abcd"), context))
        assertFalse(validator.isValid(listOf("abcde"), context))
        assertFalse(validator.isValid(listOf("one", "two", "three"), context))
    }

    @Test
    fun `default annotation limits allow twenty short tags`() {
        val limits = annotation("tags")

        assertEquals(20, limits.maxCount)
        assertEquals(32, limits.maxLength)
        assertTrue(limits.message.contains("tags"))
        assertTrue(validator.isValid(List(20) { "tag$it" }, unusedContext()))
        validator.initialize(limits)
        assertFalse(validator.isValid(List(21) { "tag$it" }, unusedContext()))
    }

    private fun annotation(fieldName: String): PromptTags =
        Tagged::class.java.getDeclaredField(fieldName).getAnnotation(PromptTags::class.java)

    private fun unusedContext(): ConstraintValidatorContext = Proxy.newProxyInstance(
        javaClass.classLoader,
        arrayOf(ConstraintValidatorContext::class.java),
        InvocationHandler { _, _, _ -> null },
    ) as ConstraintValidatorContext

    private class Tagged {
        @PromptTags
        var tags: List<String>? = null

        @PromptTags(maxCount = 2, maxLength = 4, message = "too many")
        var tiny: List<String>? = null
    }
}
