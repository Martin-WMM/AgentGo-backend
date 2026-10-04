package com.agentgo.commons.dto.http.response.status

import com.agentgo.commons.dto.http.response.status.error.ErrorResponseCodeRegistry
import com.agentgo.commons.dto.http.response.status.success.FileResponseCodeRegistry
import kotlin.test.Test
import kotlin.test.assertEquals

class ResponseCodeRegistryTest {
    @Test
    fun resolvesSplitErrorCodeDescriptions() {
        val description = ErrorResponseCodeRegistry.description("FILE-001")

        assertEquals("FILE", description?.category)
        assertEquals("File name or path is invalid.", description?.meaning)
    }

    @Test
    fun exposesSpecificSuccessCodeDescriptions() {
        assertEquals(
            "FILE",
            FileResponseCodeRegistry.codeDescriptions[FileResponseCodeRegistry.UPLOAD_COMPLETED_TYPE]?.category,
        )
    }

    @Test
    fun resolvesNumericCodeToItsResponseTypeAndDescription() {
        assertEquals("FILE-001", ResponseCodeRegistry.responseType(2001))
        assertEquals("File name or path is invalid.", ResponseCodeRegistry.description(2001)?.meaning)
    }
}
