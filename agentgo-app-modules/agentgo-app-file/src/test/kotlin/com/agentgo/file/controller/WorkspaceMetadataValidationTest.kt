package com.agentgo.file.controller

import com.agentgo.dto.workspace.WorkspaceCreateRequest
import com.agentgo.dto.workspace.WorkspaceMetadata
import com.agentgo.dto.workspace.WorkspaceUpdateRequest
import jakarta.validation.Validation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorkspaceMetadataValidationTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `create accepts an omitted color and a palette color ignoring case`() {
        val omitted = WorkspaceCreateRequest().apply { name = "Reports" }
        val palette = WorkspaceCreateRequest().apply {
            name = "Reports"
            metadata = WorkspaceMetadata().apply { color = "#0e7490" }
        }

        assertTrue(validator.validate(omitted).isEmpty())
        assertTrue(validator.validate(palette).isEmpty())
    }

    @Test
    fun `update rejects a color outside the palette`() {
        val request = WorkspaceUpdateRequest().apply {
            name = "Reports"
            metadata = WorkspaceMetadata().apply { color = "#000000" }
        }

        val violations = validator.validate(request)

        assertEquals(setOf("metadata.color"), violations.map { it.propertyPath.toString() }.toSet())
        assertEquals("must be a supported workspace color", violations.first().message)
    }

    @Test
    fun `blank color is rejected`() {
        val request = WorkspaceCreateRequest().apply {
            name = "Reports"
            metadata = WorkspaceMetadata().apply { color = "   " }
        }

        val violations = validator.validate(request)

        assertEquals("metadata.color", violations.single().propertyPath.toString())
    }
}
