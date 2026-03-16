package com.study.platform.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.study.platform.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    private ApiResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T>ResponseEntity<ApiResponse<T>> success(SuccessCode successCode, T data) {
        return ResponseEntity
                .status(successCode.getHttpStatus())
                .body(new ApiResponse<>(true, successCode.name(), successCode.getMessage(), data));
    }

    public static ResponseEntity<ApiResponse<Void>> success(SuccessCode successCode) {
        return ResponseEntity
                .status(successCode.getHttpStatus())
                .body(new ApiResponse<>(true, successCode.name(), successCode.getMessage(), null));
    }

    public static ResponseEntity<ApiResponse<Void>> fail(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ApiResponse<>(false, errorCode.name(), errorCode.getMessage(), null));
    }
}
