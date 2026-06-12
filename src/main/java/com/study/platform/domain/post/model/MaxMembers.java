package com.study.platform.domain.post.model;

import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import jakarta.persistence.Embeddable;

@Embeddable
public record MaxMembers(int value) {

    public MaxMembers {
        if (value < 1) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }
}
