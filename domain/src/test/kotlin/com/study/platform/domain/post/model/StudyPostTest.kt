package com.study.platform.domain.post.model

import com.study.platform.domain.user.model.User
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.assertj.core.api.Assertions.assertThatNoException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.UUID

class StudyPostTest {

    private lateinit var author: User
    private lateinit var post: StudyPost

    @BeforeEach
    fun setUp() {
        author = User.create("kakao-1", "작성자", "author@test.com")
        ReflectionTestUtils.setField(author, "id", UUID.randomUUID())
        post = StudyPost.create(author, "스터디 모집", "열심히 합니다", "Java", 5, LocalDateTime.now().plusDays(7))
    }

    @Test
    fun `validateOpen_OPEN상태_예외없음`() {
        // given
        // post is already OPEN from setUp

        // when & then
        assertThatNoException().isThrownBy { post.validateOpen() }
    }

    @Test
    fun `validateOpen_CLOSED상태_예외발생`() {
        // given
        post.close()

        // when & then
        assertThatThrownBy { post.validateOpen() }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.POST_CLOSED)
            }
    }

    @Test
    fun `validateOpen_FULL상태_예외발생`() {
        // given
        post.markFull()

        // when & then
        assertThatThrownBy { post.validateOpen() }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.POST_CLOSED)
            }
    }

    @Test
    fun `validateAuthor_작성자_예외없음`() {
        // given
        val authorId = author.id!!

        // when & then
        assertThatNoException().isThrownBy { post.validateAuthor(authorId) }
    }

    @Test
    fun `validateAuthor_작성자아님_예외발생`() {
        // given
        val otherId = UUID.randomUUID()

        // when & then
        assertThatThrownBy { post.validateAuthor(otherId) }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.FORBIDDEN)
            }
    }

    @Test
    fun `validateNotAuthor_작성자아님_예외없음`() {
        // given
        val otherId = UUID.randomUUID()

        // when & then
        assertThatNoException().isThrownBy { post.validateNotAuthor(otherId) }
    }

    @Test
    fun `validateNotAuthor_작성자_예외발생`() {
        // given
        val authorId = author.id!!

        // when & then
        assertThatThrownBy { post.validateNotAuthor(authorId) }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.CANNOT_APPLY_OWN_POST)
            }
    }

    @Test
    fun `markFullIfNeeded_승인인원이_정원_이상이면_FULL로_전환된다`() {
        // when
        post.markFullIfNeeded(5L)

        // then
        assertThat(post.isOpen()).isFalse()
    }

    @Test
    fun `markFullIfNeeded_승인인원이_정원_미만이면_OPEN을_유지한다`() {
        // when
        post.markFullIfNeeded(4L)

        // then
        assertThat(post.isOpen()).isTrue()
    }

    @Test
    fun `create_마감일이_과거면_예외발생`() {
        // when & then
        assertThatThrownBy {
            StudyPost.create(author, "스터디 모집", "열심히 합니다", "Java", 5, LocalDateTime.now().minusDays(1))
        }.isInstanceOfSatisfying(CustomException::class.java) { e ->
            assertThat(e.errorCode).isEqualTo(ErrorCode.INVALID_INPUT)
        }
    }

    @Test
    fun `update_마감일이_과거면_예외발생`() {
        // when & then
        assertThatThrownBy {
            post.update("수정된 제목", "수정된 내용", "Java", 5, LocalDateTime.now().minusDays(1))
        }.isInstanceOfSatisfying(CustomException::class.java) { e ->
            assertThat(e.errorCode).isEqualTo(ErrorCode.INVALID_INPUT)
        }
    }
}
