package com.agentgo.events.annotation

import com.agentgo.events.domain.SystemEventDomains

/**
 * Declares that a successful (or optionally failed) service operation should be
 * written to the global system-event log.
 *
 * ## Protocol
 *
 * 1. Annotate **mutating** application-service methods that change durable state.
 *    Do not annotate pure reads, probes, or render helpers.
 * 2. Set [domain] to a [SystemEventDomains] constant (or a new constant added there).
 * 3. Set [name] to a short past-tense action label shared with operators
 *    (for example `"Model created"`).
 * 4. Prefer SpEL templates in [comment] for durable identifiers and names.
 *    Template placeholders use `#{…}` and may reference method parameters by
 *    name, `#result` after success, and `#error` when [recordOnFailure] is true.
 * 5. Leave [operator] blank so the authenticated principal is stored, unless the
 *    business actor differs from the caller.
 * 6. Recording runs **after** the method returns successfully. Failures of the
 *    recorder never change the business outcome.
 * 7. For control flow that an annotation cannot express, inject
 *    [com.agentgo.events.service.SystemEventRecorder] and call it explicitly.
 *
 * ## Example
 *
 * ```kotlin
 * @RecordSystemEvent(
 *     domain = SystemEventDomains.MODELS,
 *     name = "Model created",
 *     comment = "Created model #{#result.name} (#{#result.id})",
 * )
 * fun create(request: ModelCreateRequest): ModelResponse { … }
 * ```
 *
 * @property domain business domain; prefer [SystemEventDomains]
 * @property name human-readable event name; supports `#{…}` SpEL templates
 * @property comment optional comment; supports `#{…}` SpEL templates
 * @property operator optional operator override; blank keeps the authenticated user;
 * supports `#{…}` SpEL templates
 * @property condition SpEL boolean; blank means always record; supports raw SpEL
 * (not a template), for example `#result != null`
 * @property recordOnFailure when true, also record after the method throws; `#error`
 * is then available in templates
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class RecordSystemEvent(
    val domain: String,
    val name: String,
    val comment: String = "",
    val operator: String = "",
    val condition: String = "",
    val recordOnFailure: Boolean = false,
)
