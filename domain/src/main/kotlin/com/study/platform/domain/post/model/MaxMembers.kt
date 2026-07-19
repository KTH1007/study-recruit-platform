package com.study.platform.domain.post.model

import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import jakarta.persistence.Embeddable

@Embeddable
class MaxMembers(val value: Int) {

    companion object {
        private const val MAX_VALUE = 100
    }

    init {
        if (value < 1 || value > MAX_VALUE) throw CustomException(ErrorCode.INVALID_INPUT)
    }
}
