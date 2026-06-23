package com.study.platform.domain.user.model

import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import jakarta.persistence.Embeddable
import java.util.regex.Pattern

@Embeddable
class Email(val value: String) {
    companion object {
        private val EMAIL_PATTERN = Pattern.compile("^[\\w\\-.]+@([\\w\\-]+\\.)+[\\w\\-]{2,}$")
    }
    init {
        if (!EMAIL_PATTERN.matcher(value).matches()) throw CustomException(ErrorCode.INVALID_INPUT)
    }
}
