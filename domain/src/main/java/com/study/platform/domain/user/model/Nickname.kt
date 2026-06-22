package com.study.platform.domain.user.model

import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import jakarta.persistence.Embeddable

@Embeddable
class Nickname(val value: String) {
    init {
        if (value.isBlank()) throw CustomException(ErrorCode.INVALID_INPUT)
        if (value.length > 50) throw CustomException(ErrorCode.INVALID_INPUT)
    }
}
