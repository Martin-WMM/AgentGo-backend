package com.agentgo.models.controller

import com.agentgo.commons.dto.http.response.status.success.ModelsResponseCodeRegistry
import com.agentgo.dto.models.ModelCreateRequest
import com.agentgo.dto.models.ModelResponse
import com.agentgo.dto.models.ModelType
import com.agentgo.dto.models.ModelUpdateRequest
import com.agentgo.models.service.ModelService
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus

class ModelControllerTest {
    private val modelService = mock<ModelService>()
    private val controller = ModelController(modelService)

    @Test
    fun delegatesCrudOperationsAndWrapsSuccessCodes() {
        val id = UUID.randomUUID()
        val createRequest = ModelCreateRequest("gpt", "openai", ModelType.LLM)
        val updateRequest = ModelUpdateRequest("gpt", "openai", ModelType.LLM, emptyMap(), "desc")
        val response = ModelResponse(
            id = id,
            name = "gpt",
            provider = "openai",
            type = ModelType.LLM,
            configuration = emptyMap(),
            description = "desc",
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-01-02T00:00:00Z"),
        )
        whenever(modelService.create(createRequest)).thenReturn(response)
        whenever(modelService.get(id)).thenReturn(response)
        whenever(modelService.list(ModelType.LLM)).thenReturn(listOf(response))
        whenever(modelService.update(id, updateRequest)).thenReturn(response)

        val created = controller.create(createRequest)
        val retrieved = controller.get(id)
        val listed = controller.list(ModelType.LLM)
        val updated = controller.update(id, updateRequest)
        val deleted = controller.delete(id)

        assertEquals(ModelsResponseCodeRegistry.CREATED, created.code)
        assertEquals(ModelsResponseCodeRegistry.RETRIEVED, retrieved.code)
        assertEquals(ModelsResponseCodeRegistry.LIST_RETRIEVED, listed.code)
        assertEquals(ModelsResponseCodeRegistry.UPDATED, updated.code)
        assertEquals(response, created.data)
        assertEquals(listOf(response), listed.data)
        assertEquals(HttpStatus.NO_CONTENT, deleted.statusCode)
        assertNull(deleted.body)
        verify(modelService).delete(id)
    }
}
