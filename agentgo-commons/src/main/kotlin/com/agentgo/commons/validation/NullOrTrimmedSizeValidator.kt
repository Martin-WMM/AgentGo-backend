package com.agentgo.commons.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/** Hibernate Validator implementation of [NullOrTrimmedSize]. */
class NullOrTrimmedSizeValidator : ConstraintValidator<NullOrTrimmedSize, CharSequence?> {
    private var min: Int = 0
    private var max: Int = Int.MAX_VALUE

    override fun initialize(constraintAnnotation: NullOrTrimmedSize) {
        min = constraintAnnotation.min
        max = constraintAnnotation.max
        require(min >= 0) { "min must be >= 0" }
        require(max >= min) { "max must be >= min" }
    }

    override fun isValid(value: CharSequence?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true
        }
        val trimmed = value.toString().trim()
        if (trimmed.isEmpty()) {
            return true
        }
        return trimmed.length in min..max
    }
}
