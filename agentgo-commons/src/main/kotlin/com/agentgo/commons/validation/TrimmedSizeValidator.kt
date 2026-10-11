package com.agentgo.commons.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/** Hibernate Validator implementation of [TrimmedSize]. */
class TrimmedSizeValidator : ConstraintValidator<TrimmedSize, CharSequence?> {
    private var min: Int = 0
    private var max: Int = Int.MAX_VALUE

    override fun initialize(constraintAnnotation: TrimmedSize) {
        min = constraintAnnotation.min
        max = constraintAnnotation.max
        require(min >= 0) { "min must be >= 0" }
        require(max >= min) { "max must be >= min" }
    }

    override fun isValid(value: CharSequence?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true
        }
        val length = value.toString().trim().length
        return length in min..max
    }
}
