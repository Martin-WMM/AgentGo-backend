package com.agentgo.commons.annotation

/**
 * Marks a controller API whose request and successful response should be logged
 * by the AgentGo starter logging aspect.
 *
 * When [name] is blank, the annotated method name is used.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AgentGoLogApi(
    val name: String = "",
)
