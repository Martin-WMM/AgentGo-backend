package com.agentgo.dto.presentation

import jakarta.validation.ConstraintValidatorContext
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResourceIconTest {
    private val validator = ResourceIconValidator()

    @Test
    fun `canonicalizes iconify ids and absolute urls`() {
        val iconify = ResourceIcons.canonicalize(icon(IconSource.ICONIFY, " lucide:bot "))
        val url = ResourceIcons.canonicalize(icon(IconSource.URL, "https://cdn.example.com/bot.png"))

        assertEquals(IconSource.ICONIFY, iconify?.source)
        assertEquals("lucide:bot", iconify?.value)
        assertEquals("https://cdn.example.com/bot.png", url?.value)
        assertNull(ResourceIcons.canonicalize(null))
    }

    @Test
    fun `rejects a missing source a blank value and an unsupported reference`() {
        assertFailsWith<IllegalArgumentException> {
            ResourceIcons.canonicalize(icon(null, "lucide:bot"))
        }
        assertFailsWith<IllegalArgumentException> {
            ResourceIcons.canonicalize(icon(IconSource.ICONIFY, "   "))
        }
        assertFailsWith<IllegalArgumentException> {
            ResourceIcons.canonicalize(icon(IconSource.ICONIFY, "Bot"))
        }
        assertFailsWith<IllegalArgumentException> {
            ResourceIcons.canonicalize(icon(IconSource.URL, "ftp://cdn.example.com/bot.png"))
        }
        assertFailsWith<IllegalArgumentException> {
            ResourceIcons.canonicalize(icon(IconSource.URL, "https://"))
        }
    }

    @Test
    fun `bean validation allows a missing icon and rejects a bad one`() {
        assertTrue(validator.isValid(null, unusedContext()))
        assertTrue(validator.isValid(icon(IconSource.ICONIFY, "mdi:home"), unusedContext()))
        assertFalse(validator.isValid(icon(IconSource.URL, "not a url"), unusedContext()))
    }

    @Test
    fun `duplicate and equality follow source and value`() {
        val original = icon(IconSource.ICONIFY, "lucide:bot")
        val copy = original.duplicate()

        assertEquals(original, copy)
        assertEquals(original.hashCode(), copy.hashCode())
        assertEquals(original, original)
        assertNotEquals(original, icon(IconSource.URL, "https://cdn.example.com/bot.png"))
        assertFalse(original.equals("lucide:bot"))
        assertEquals("ResourceIcon(source=ICONIFY, value=lucide:bot)", original.toString())
    }

    private fun icon(source: IconSource?, value: String) = ResourceIcon().apply {
        this.source = source
        this.value = value
    }

    private fun unusedContext(): ConstraintValidatorContext = Proxy.newProxyInstance(
        javaClass.classLoader,
        arrayOf(ConstraintValidatorContext::class.java),
        InvocationHandler { _, _, _ -> null },
    ) as ConstraintValidatorContext
}
