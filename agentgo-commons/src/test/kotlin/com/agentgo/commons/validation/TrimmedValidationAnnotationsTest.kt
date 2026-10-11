package com.agentgo.commons.validation

import jakarta.validation.Validation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrimmedValidationAnnotationsTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun trimmedNotBlankRejectsBlankValues() {
        data class Sample(
            @field:TrimmedNotBlank
            val name: String?,
        )

        assertEquals(1, validator.validate(Sample(null)).size)
        assertEquals(1, validator.validate(Sample("   ")).size)
        assertTrue(validator.validate(Sample("Reports")).isEmpty())
    }

    @Test
    fun trimmedSizeUsesTrimmedLength() {
        data class Sample(
            @field:TrimmedSize(max = 5)
            val name: String?,
        )

        assertTrue(validator.validate(Sample(null)).isEmpty())
        assertTrue(validator.validate(Sample("  abc  ")).isEmpty())
        assertEquals(1, validator.validate(Sample("abcdef")).size)
    }

    @Test
    fun nullOrTrimmedSizeAllowsBlankOptionalText() {
        data class Sample(
            @field:NullOrTrimmedSize(max = 5)
            val description: String?,
        )

        assertTrue(validator.validate(Sample(null)).isEmpty())
        assertTrue(validator.validate(Sample("   ")).isEmpty())
        assertTrue(validator.validate(Sample("notes")).isEmpty())
        assertEquals(1, validator.validate(Sample("too-long")).size)
    }
}
