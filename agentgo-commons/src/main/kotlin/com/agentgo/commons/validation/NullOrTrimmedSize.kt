package com.agentgo.commons.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

/**
 * Validates an optional character sequence.
 *
 * `null`, blank, and whitespace-only values are accepted. Non-blank values must have a
 * trimmed length within `[min, max]`.
 */
@MustBeDocumented
@Target(FIELD, VALUE_PARAMETER, ANNOTATION_CLASS, PROPERTY_GETTER)
@Retention(RUNTIME)
@Constraint(validatedBy = [NullOrTrimmedSizeValidator::class])
annotation class NullOrTrimmedSize(
    val min: Int = 0,
    val max: Int = Int.MAX_VALUE,
    val message: String = "optional trimmed length must be between {min} and {max}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
