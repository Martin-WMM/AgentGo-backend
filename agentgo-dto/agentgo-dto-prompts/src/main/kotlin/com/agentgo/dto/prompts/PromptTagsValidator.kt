package com.agentgo.dto.prompts

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

/** Applies [PromptTags] to a list of display tags. */
class PromptTagsValidator : ConstraintValidator<PromptTags, List<String>?> {
    private var maxCount: Int = 20
    private var maxLength: Int = 32

    override fun initialize(constraintAnnotation: PromptTags) {
        maxCount = constraintAnnotation.maxCount
        maxLength = constraintAnnotation.maxLength
    }

    override fun isValid(value: List<String>?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        val tags = linkedSetOf<String>()
        for (raw in value) {
            val tag = raw.trim()
            if (tag.isEmpty()) continue
            if (tag.length > maxLength || tag.any { it.isISOControl() || it == ',' }) return false
            tags += tag
            if (tags.size > maxCount) return false
        }
        return true
    }
}
