package com.study.platform.global.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.study.platform.global.exception.ErrorCode
import org.springframework.http.ResponseEntity

@JsonInclude(JsonInclude.Include.NON_NULL)
class ApiResponse<T> private constructor(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T?
) {
    companion object {
        fun <T> success(successCode: SuccessCode, data: T): ResponseEntity<ApiResponse<T>> =
            ResponseEntity
                .status(successCode.httpStatus)
                .body(ApiResponse(true, successCode.name, successCode.message, data))

        fun success(successCode: SuccessCode): ResponseEntity<ApiResponse<Void>> =
            ResponseEntity
                .status(successCode.httpStatus)
                .body(ApiResponse(true, successCode.name, successCode.message, null))

        fun fail(errorCode: ErrorCode): ResponseEntity<ApiResponse<Void>> =
            ResponseEntity
                .status(errorCode.httpStatus)
                .body(ApiResponse(false, errorCode.name, errorCode.message, null))

        fun noContent(): ResponseEntity<Void> = ResponseEntity.noContent().build()
    }
}
