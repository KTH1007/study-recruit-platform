package com.study.platform.domain.post.model

import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.assertj.core.api.Assertions.assertThatNoException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test

class MaxMembersTest {

    @Test
    fun `유효한_범위면_예외없음`() {
        assertThatNoException().isThrownBy { MaxMembers(1) }
        assertThatNoException().isThrownBy { MaxMembers(100) }
    }

    @Test
    fun `1미만이면_예외발생`() {
        assertThatThrownBy { MaxMembers(0) }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.INVALID_INPUT)
            }
    }

    @Test
    fun `100초과면_예외발생`() {
        assertThatThrownBy { MaxMembers(101) }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.INVALID_INPUT)
            }
    }
}
