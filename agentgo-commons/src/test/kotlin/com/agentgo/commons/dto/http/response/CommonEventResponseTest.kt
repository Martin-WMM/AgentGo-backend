package com.agentgo.commons.dto.http.response

import kotlin.test.Test
import kotlin.test.assertTrue

class CommonEventResponseTest {
    @Test
    fun assignsEventIdWhenOneIsNotProvided() {
        val response = CommonEventResponse<Unit>(
            eventName = "progress",
            namespace = "agent",
            requestId = "request-1",
            eventType = "status",
        )

        assertTrue(response.eventId.isNotBlank())
    }
}
