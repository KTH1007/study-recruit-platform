package com.study.platform.domain.comment.model

import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.user.model.User
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.support.fake.FakeDomainEventPublisher
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.assertj.core.api.AssertionsForClassTypes.assertThatNoException
import org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.UUID

class CommentTest {

    private lateinit var author: User
    private lateinit var post: StudyPost
    private lateinit var comment: Comment

    @BeforeEach
    fun setUp() {
        author = User.create("kakao-1", "작성자", "author@test.com")
        ReflectionTestUtils.setField(author, "id", UUID.randomUUID())

        post = StudyPost.create(author, "스터디 모집", "열심히 합니다", "Java", 5, LocalDateTime.now().plusDays(7))
        ReflectionTestUtils.setField(post, "id", UUID.randomUUID())

        comment = Comment.create(post, author, "댓글 내용", FakeDomainEventPublisher())
    }

    @Test
    fun `validateAuthor_작성자_예외없음`() {
        // given
        val authorId = author.id!!

        // when & then
        assertThatNoException().isThrownBy { comment.validateAuthor(authorId) }
    }

    @Test
    fun `validateAuthor_작성자아님_예외발생`() {
        // given
        val otherId = UUID.randomUUID()

        // when & then
        assertThatThrownBy { comment.validateAuthor(otherId) }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.NOT_COMMENT_AUTHOR)
            }
    }

    @Test
    fun `create_내용이_빈문자열이면_예외발생`() {
        assertThatThrownBy { Comment.create(post, author, "   ", FakeDomainEventPublisher()) }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.INVALID_INPUT)
            }
    }

    @Test
    fun `create_내용이_500자_초과면_예외발생`() {
        val tooLong = "a".repeat(501)

        assertThatThrownBy { Comment.create(post, author, tooLong, FakeDomainEventPublisher()) }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.INVALID_INPUT)
            }
    }

    @Test
    fun `update_내용이_빈문자열이면_예외발생`() {
        assertThatThrownBy { comment.update("") }
            .isInstanceOfSatisfying(CustomException::class.java) { e ->
                assertThat(e.errorCode).isEqualTo(ErrorCode.INVALID_INPUT)
            }
    }
}
