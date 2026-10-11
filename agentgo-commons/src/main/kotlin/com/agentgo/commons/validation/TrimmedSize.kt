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
 * Validates the trimmed length of a character sequence.
 *
 * `null` values are considered valid so this annotation can be paired with
 * [TrimmedNotBlank] or [jakarta.validation.constraints.NotNull] as needed.
 */
@MustBeDocumented
@Target(FIELD, VALUE_PARAMETER, ANNOTATION_CLASS, PROPERTY_GETTER)
@Retention(RUNTIME)
@Constraint(validatedBy = [TrimmedSizeValidator::class])
annotation class TrimmedSize(
    val min: Int = 0,
    val max: Int = Int.MAX_VALUE,
    val message: String = "trimmed length must be between {min} and {max}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
