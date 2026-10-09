package com.agentgo.models.exception

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class ModelErrorResponsesTest {
    @Test
    fun mapsDomainExceptionsOntoCommonHttpResponse() {
        val response = ModelErrorResponses.of(ModelNotFoundException())

        assertEquals(8001, response.code)
        assertEquals("MODEL-001", response.responseType)
        assertEquals("The requested model was not found.", response.message)
        assertFalse(response.success)
        assertFalse(response.retryable)
        assertEquals("MODEL", assertIs<ResponseCodeDescription>(response.error).category)
    }
}
