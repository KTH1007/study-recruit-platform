package com.study.platform.domain.comment.model

import com.study.platform.domain.comment.dto.response.CommentResponse
import com.study.platform.domain.comment.port.CommentQueryPort
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.user.model.User
import com.study.platform.domain.user.model.UserRepository
import com.study.platform.global.support.AbstractIntegrationTest
import com.study.platform.support.fake.FakeDomainEventPublisher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Transactional
class CommentRepositoryTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var commentRepository: CommentRepository

    @Autowired
    private lateinit var commentQueryPort: CommentQueryPort

    @Autowired
    private lateinit var studyPostRepository: StudyPostRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var author: User
    private lateinit var post: StudyPost
    private lateinit var comment: Comment

    @BeforeEach
    fun setUp() {
        author = userRepository.save(User.create("kakao-1", "작성자", "author@test.com"))
        post = studyPostRepository.save(
            StudyPost.create(author, "스터디 모집", "열심히 합니다", "Java", 5, LocalDateTime.now().plusDays(7))
        )
        comment = commentRepository.save(Comment.create(post, author, "좋은 스터디네요", FakeDomainEventPublisher()))
        em.flush()
    }

    @Test
    fun `findAllByPostId_성공`() {
        val result: Page<CommentResponse> = commentQueryPort.findAllByPostId(post.id!!, PageRequest.of(0, 20))

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].content).isEqualTo("좋은 스터디네요")
        assertThat(result.content[0].authorNickname).isEqualTo("작성자")
    }

    @Test
    fun `findAllByPostId_댓글없음_빈페이지`() {
        val otherPost = studyPostRepository.save(
            StudyPost.create(author, "다른 스터디", "열심히 합니다", "Kotlin", 3, LocalDateTime.now().plusDays(7))
        )

        val result: Page<CommentResponse> = commentQueryPort.findAllByPostId(otherPost.id!!, PageRequest.of(0, 20))

        assertThat(result.content).isEmpty()
        assertThat(result.totalElements).isZero()
    }

    @Test
    fun `findAllByPostId_페이징_성공`() {
        commentRepository.save(Comment.create(post, author, "두 번째 댓글", FakeDomainEventPublisher()))
        commentRepository.save(Comment.create(post, author, "세 번째 댓글", FakeDomainEventPublisher()))
        em.flush()

        val firstPage: Page<CommentResponse> = commentQueryPort.findAllByPostId(post.id!!, PageRequest.of(0, 2))
        val secondPage: Page<CommentResponse> = commentQueryPort.findAllByPostId(post.id!!, PageRequest.of(1, 2))

        assertThat(firstPage.content).hasSize(2)
        assertThat(secondPage.content).hasSize(1)
        assertThat(firstPage.totalElements).isEqualTo(3)
    }

    @Test
    fun `findByIdWithAuthor_성공`() {
        val result: Comment? = commentRepository.findByIdWithAuthor(comment.id!!)

        assertThat(result).isNotNull()
        assertThat(result!!.content).isEqualTo("좋은 스터디네요")
        assertThat(result.author?.nickname).isEqualTo("작성자")
    }

    @Test
    fun `findByIdWithAuthor_존재하지않음_빈Optional`() {
        val result: Comment? = commentRepository.findByIdWithAuthor(UUID.randomUUID())

        assertThat(result).isNull()
    }
}
