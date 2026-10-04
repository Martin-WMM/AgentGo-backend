package com.agentgo.starter.annotation

/**
 * Marks a controller API whose request and successful response should be logged.
 *
 * When [name] is blank, the annotated method name is used.
 *
 * @author Martin M. W.
 * @version 0.1.0-SNAPSHOT
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AgentGoLogApi(
    val name: String = "",
)
