package com.study.platform.global.exception

import com.study.platform.global.response.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<ApiResponse<Void>> {
        log.warn("CustomException: {}", e.message)
        return ApiResponse.fail(e.errorCode)
    }

    @ExceptionHandler(PessimisticLockingFailureException::class)
    fun handlePessimisticLockingFailureException(e: PessimisticLockingFailureException): ResponseEntity<ApiResponse<Void>> {
        log.warn("PessimisticLockingFailureException: {}", e.message, e)
        return ApiResponse.fail(ErrorCode.LOCK_CONFLICT)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Void>> {
        val fieldError: FieldError? = e.bindingResult.fieldErrors.firstOrNull()
        val message = if (fieldError != null) {
            "${fieldError.field}: ${fieldError.defaultMessage}"
        } else {
            e.bindingResult.allErrors.firstOrNull()?.defaultMessage ?: "유효성 검증에 실패했습니다."
        }
        log.warn("ValidationException: {}", message)
        return ApiResponse.fail(ErrorCode.INVALID_INPUT)
    }

    @ExceptionHandler(
        HttpMessageNotReadableException::class,
        MethodArgumentTypeMismatchException::class,
        MissingServletRequestParameterException::class
    )
    fun handleBadRequestException(e: Exception): ResponseEntity<ApiResponse<Void>> {
        log.warn("{}: {}", e.javaClass.simpleName, e.message)
        return ApiResponse.fail(ErrorCode.INVALID_INPUT)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Void>> {
        log.error("UnhandledException: {}", e.message, e)
        return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR)
    }
}
