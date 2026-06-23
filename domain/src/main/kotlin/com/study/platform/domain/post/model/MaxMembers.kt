package com.study.platform.domain.post.model

import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import jakarta.persistence.Embeddable

@Embeddable
class MaxMembers(val value: Int) {
    init {
        if (value < 1) throw CustomException(ErrorCode.INVALID_INPUT)
    }
}
