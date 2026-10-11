package com.agentgo.starter

import com.agentgo.commons.dto.http.response.ValidationFieldError
import com.agentgo.starter.i18n.AgentGoMessages
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.StaticMessageSource
import org.springframework.core.MethodParameter
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Path
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException

class ValidationExceptionHandlerTest {
    private val messageSource = StaticMessageSource().apply {
        addMessage("VALIDATION-002", Locale.ENGLISH, "One or more fields have an invalid format.")
        addMessage("VALIDATION-002", Locale.SIMPLIFIED_CHINESE, "一个或多个字段格式无效。")
        addMessage("validation.field.invalid", Locale.ENGLISH, "invalid value")
    }
    private val handler = ValidationExceptionHandler(AgentGoMessages(messageSource))

    @AfterTest
    fun clearLocale() {
        LocaleContextHolder.resetLocaleContext()
    }

    @Test
    fun `maps method argument not valid to validation envelope with english message`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val target = Any()
        val bindingResult = BeanPropertyBindingResult(target, "workspaceCreateRequest")
        bindingResult.addError(FieldError("workspaceCreateRequest", "name", "must not be blank after trimming"))
        val method = SampleController::class.java.getDeclaredMethod("create", String::class.java)
        val exception = MethodArgumentNotValidException(MethodParameter(method, 0), bindingResult)

        val response = handler.handleMethodArgumentNotValid(exception)

        assertEquals(400, response.statusCode.value())
        assertEquals(3002, response.body?.code)
        assertEquals("VALIDATION-002", response.body?.responseType)
        assertEquals("One or more fields have an invalid format.", response.body?.message)
        assertFalse(response.body?.success == true)
        val error = response.body?.error as Map<*, *>
        val fields = error["fields"] as List<*>
        assertEquals(1, fields.size)
        assertEquals("name", (fields.first() as ValidationFieldError).field)
    }

    @Test
    fun `maps method argument not valid to chinese message when locale is zh-CN`() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE)
        val target = Any()
        val bindingResult = BeanPropertyBindingResult(target, "workspaceCreateRequest")
        bindingResult.addError(FieldError("workspaceCreateRequest", "name", "must not be blank after trimming"))
        val method = SampleController::class.java.getDeclaredMethod("create", String::class.java)
        val exception = MethodArgumentNotValidException(MethodParameter(method, 0), bindingResult)

        val response = handler.handleMethodArgumentNotValid(exception)

        assertEquals("一个或多个字段格式无效。", response.body?.message)
    }

    @Test
    fun `uses the field fallback when a validation message is missing`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val bindingResult = BeanPropertyBindingResult(Any(), "form")
        bindingResult.addError(FieldError("form", "name", null, false, null, null, null))
        val method = SampleController::class.java.getDeclaredMethod("create", String::class.java)

        val response = handler.handleMethodArgumentNotValid(
            MethodArgumentNotValidException(MethodParameter(method, 0), bindingResult),
        )

        val fields = (response.body?.error as Map<*, *>)["fields"] as List<*>
        assertEquals("invalid value", (fields.first() as ValidationFieldError).message)
    }

    @Test
    fun `maps bind exceptions and substitutes a request error when no field failed`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val empty = BeanPropertyBindingResult(Any(), "form")
        empty.addError(ObjectError("form", "bad"))

        val emptyResponse = handler.handleBindException(org.springframework.validation.BindException(empty))
        val emptyFields = (emptyResponse.body?.error as Map<*, *>)["fields"] as List<*>
        assertEquals("request", (emptyFields.first() as ValidationFieldError).field)

        val bindingResult = BeanPropertyBindingResult(Any(), "form")
        bindingResult.addError(FieldError("form", "email", "invalid email"))
        val response = handler.handleBindException(org.springframework.validation.BindException(bindingResult))
        val fields = (response.body?.error as Map<*, *>)["fields"] as List<*>
        assertEquals("email", (fields.first() as ValidationFieldError).field)
        assertEquals("invalid email", (fields.first() as ValidationFieldError).message)
    }

    @Test
    fun `maps constraint violations and blank paths to the request field`() {
        val named = handler.handleConstraintViolation(
            ConstraintViolationException(setOf(violation("email", "invalid email"))),
        )
        val blank = handler.handleConstraintViolation(
            ConstraintViolationException(setOf(violation("  ", "invalid"))),
        )
        val missing = handler.handleConstraintViolation(ConstraintViolationException(emptySet()))

        val namedFields = (named.body?.error as Map<*, *>)["fields"] as List<*>
        assertEquals("email", (namedFields.first() as ValidationFieldError).field)
        val blankFields = (blank.body?.error as Map<*, *>)["fields"] as List<*>
        assertEquals("request", (blankFields.first() as ValidationFieldError).field)
        val missingFields = (missing.body?.error as Map<*, *>)["fields"] as List<*>
        assertEquals("The request body is invalid.", (missingFields.first() as ValidationFieldError).message)
    }

    private fun violation(path: String, message: String): ConstraintViolation<Any> {
        val propertyPath = Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(Path::class.java),
            InvocationHandler { proxy, method, _ ->
                when (method.name) {
                    "toString" -> path
                    "hashCode" -> System.identityHashCode(proxy)
                    "equals" -> false
                    else -> null
                }
            },
        ) as Path
        return Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(ConstraintViolation::class.java),
            InvocationHandler { proxy, method, args ->
                when (method.name) {
                    "getPropertyPath" -> propertyPath
                    "getMessage" -> message
                    "hashCode" -> System.identityHashCode(proxy)
                    "equals" -> args?.firstOrNull() === proxy
                    "toString" -> "ConstraintViolation($path=$message)"
                    else -> null
                }
            },
        ) as ConstraintViolation<Any>
    }

    private class SampleController {
        @Suppress("UNUSED_PARAMETER")
        fun create(name: String) = Unit
    }
}
