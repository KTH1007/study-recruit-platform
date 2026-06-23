package com.study.platform.domain.post.model

import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import jakarta.persistence.Embeddable

@Embeddable
class PostTitle(val value: String) {
    init {
        if (value.isBlank()) throw CustomException(ErrorCode.INVALID_INPUT)
        if (value.length > 100) throw CustomException(ErrorCode.INVALID_INPUT)
    }
}
