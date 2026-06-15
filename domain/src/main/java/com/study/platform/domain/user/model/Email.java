package com.study.platform.domain.user.model;

import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import jakarta.persistence.Embeddable;

import java.util.regex.Pattern;

@Embeddable
public record Email(String value) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w\\-.]+@([\\w\\-]+\\.)+[\\w\\-]{2,}$");

    public Email {
        if (value == null || !EMAIL_PATTERN.matcher(value).matches()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }
}
