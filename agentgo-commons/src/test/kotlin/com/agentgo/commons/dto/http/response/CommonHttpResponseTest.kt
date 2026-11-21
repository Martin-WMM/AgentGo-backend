package com.agentgo.commons.dto.http.response

import com.agentgo.commons.dto.http.response.status.success.BaseResponseCodeRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CommonHttpResponseTest {
    @Test
    fun createsSuccessfulResponse() {
        val response = CommonHttpResponse(code = BaseResponseCodeRegistry.SUCCESS, responseType = BaseResponseCodeRegistry.SUCCESS_TYPE, message = "OK", data = "agent-1", requestId = "request-1")

        assertTrue(response.success)
        assertEquals("agent-1", response.data)
        assertEquals("request-1", response.requestId)
    }

    @Test
    fun rejectsFailedResponseWithoutErrorDetails() {
        assertFailsWith<IllegalArgumentException> {
            CommonHttpResponse<Nothing>(code = 7001, responseType = "SYSTEM-001", message = "Failure", success = false)
        }
    }
}
