package com.agentgo.commons.http

/** A link to a related resource or an action available to the client. */
data class HttpLink(
    val rel: String,
    val href: String,
    val method: String? = null,
)
