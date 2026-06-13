package com.study.platform.domain.post.model;

import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import jakarta.persistence.Embeddable;

@Embeddable
public record PostTitle(String value) {

    public PostTitle {
        if (value == null || value.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (value.length() > 100) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }
}
