package com.study.platform.domain.comment.application

import com.study.platform.domain.comment.dto.request.CommentCreateRequest
import com.study.platform.domain.comment.dto.request.CommentUpdateRequest
import com.study.platform.domain.comment.dto.response.CommentResponse
import com.study.platform.domain.comment.event.CommentCreatedEvent
import com.study.platform.domain.comment.event.MentionEvent
import com.study.platform.domain.comment.model.Comment
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.user.model.User
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.support.fake.FakeCommentQueryPort
import com.study.platform.support.fake.FakeCommentRepository
import com.study.platform.support.fake.FakeDomainEventPublisher
import com.study.platform.support.fake.FakeStudyPostRepository
import com.study.platform.support.fake.FakeUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.UUID

class CommentServiceTest {

    private lateinit var commentRepository: FakeCommentRepository
    private lateinit var studyPostRepository: FakeStudyPostRepository
    private lateinit var userRepository: FakeUserRepository
    private lateinit var eventPublisher: FakeDomainEventPublisher

    private lateinit var createCommentService: CreateCommentService
    private lateinit var updateCommentService: UpdateCommentService
    private lateinit var deleteCommentService: DeleteCommentService

    private lateinit var authorId: UUID
    private lateinit var commenterId: UUID
    private lateinit var postId: UUID
    private lateinit var author: User
    private lateinit var commenter: User
    private lateinit var post: StudyPost

    @BeforeEach
    fun setUp() {
        commentRepository = FakeCommentRepository()
        studyPostRepository = FakeStudyPostRepository()
        userRepository = FakeUserRepository()
        eventPublisher = FakeDomainEventPublisher()

        createCommentService = CreateCommentService(commentRepository, studyPostRepository, userRepository, eventPublisher)
        updateCommentService = UpdateCommentService(commentRepository)
        deleteCommentService = DeleteCommentService(commentRepository)

        authorId = UUID.randomUUID()
        commenterId = UUID.randomUUID()
        postId = UUID.randomUUID()

        author = User.create("kakao1", "작성자", "author@test.com")
        ReflectionTestUtils.setField(author, "id", authorId)

        commenter = User.create("kakao2", "댓글작성자", "commenter@test.com")
        ReflectionTestUtils.setField(commenter, "id", commenterId)

        post = StudyPost.create(author, "스터디 모집", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7))
        ReflectionTestUtils.setField(post, "id", postId)

        studyPostRepository.save(post)
        userRepository.save(author)
        userRepository.save(commenter)
    }

    @Test
    fun `createComment_성공`() {
        // given
        val request = CommentCreateRequest("좋은 스터디네요")

        // when
        val response: CommentResponse = createCommentService.execute(commenterId, postId, request)

        // then
        assertThat(response.content).isEqualTo("좋은 스터디네요")
        assertThat(eventPublisher.hasEventOf(CommentCreatedEvent::class.java)).isTrue()
    }

    @Test
    fun `createComment_게시글없음_예외발생`() {
        // given
        val request = CommentCreateRequest("좋은 스터디네요")

        // when & then
        assertThatThrownBy { createCommentService.execute(commenterId, UUID.randomUUID(), request) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND)
    }

    @Test
    fun `createComment_사용자없음_예외발생`() {
        // given
        val request = CommentCreateRequest("좋은 스터디네요")

        // when & then
        assertThatThrownBy { createCommentService.execute(UUID.randomUUID(), postId, request) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun `createComment_작성자댓글시_방장알림미발행`() {
        // given
        val request = CommentCreateRequest("제 글에 댓글 달아요")

        // when
        createCommentService.execute(authorId, postId, request)

        // then
        assertThat(eventPublisher.hasEventOf(CommentCreatedEvent::class.java)).isFalse()
    }

    @Test
    fun `createComment_멘션포함시_멘션알림발행`() {
        // given
        val mentionedUser = User.create("kakao3", "멘션대상", "mentioned@test.com")
        ReflectionTestUtils.setField(mentionedUser, "id", UUID.randomUUID())
        userRepository.save(mentionedUser)
        val request = CommentCreateRequest("@멘션대상 확인해주세요")

        // when
        createCommentService.execute(commenterId, postId, request)

        // then
        assertThat(eventPublisher.hasEventOf(MentionEvent::class.java)).isTrue()
    }

    @Test
    fun `createComment_본인멘션시_멘션알림미발행`() {
        // given
        val request = CommentCreateRequest("@댓글작성자 본인 멘션")

        // when
        createCommentService.execute(commenterId, postId, request)

        // then
        assertThat(eventPublisher.hasEventOf(MentionEvent::class.java)).isFalse()
    }

    @Test
    fun `updateComment_성공`() {
        // given
        val comment = Comment.create(post, commenter, "원본 댓글", eventPublisher)
        val commentId = UUID.randomUUID()
        ReflectionTestUtils.setField(comment, "id", commentId)
        commentRepository.save(comment)
        val request = CommentUpdateRequest("수정된 댓글")

        // when
        val response: CommentResponse = updateCommentService.execute(commenterId, commentId, request)

        // then
        assertThat(response.content).isEqualTo("수정된 댓글")
    }

    @Test
    fun `updateComment_작성자아님_예외발생`() {
        // given
        val comment = Comment.create(post, commenter, "원본 댓글", eventPublisher)
        val commentId = UUID.randomUUID()
        ReflectionTestUtils.setField(comment, "id", commentId)
        commentRepository.save(comment)
        val request = CommentUpdateRequest("수정된 댓글")

        // when & then
        assertThatThrownBy { updateCommentService.execute(UUID.randomUUID(), commentId, request) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_COMMENT_AUTHOR)
    }

    @Test
    fun `updateComment_댓글없음_예외발생`() {
        // given
        val request = CommentUpdateRequest("수정된 댓글")

        // when & then
        assertThatThrownBy { updateCommentService.execute(commenterId, UUID.randomUUID(), request) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND)
    }

    @Test
    fun `deleteComment_성공`() {
        // given
        val comment = Comment.create(post, commenter, "삭제할 댓글", eventPublisher)
        val commentId = UUID.randomUUID()
        ReflectionTestUtils.setField(comment, "id", commentId)
        commentRepository.save(comment)

        // when
        deleteCommentService.execute(commenterId, commentId)

        // then
        assertThat(commentRepository.findByIdWithAuthor(commentId)).isNull()
    }

    @Test
    fun `deleteComment_작성자아님_예외발생`() {
        // given
        val comment = Comment.create(post, commenter, "삭제할 댓글", eventPublisher)
        val commentId = UUID.randomUUID()
        ReflectionTestUtils.setField(comment, "id", commentId)
        commentRepository.save(comment)

        // when & then
        assertThatThrownBy { deleteCommentService.execute(UUID.randomUUID(), commentId) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_COMMENT_AUTHOR)
    }

    @Test
    fun `deleteComment_댓글없음_예외발생`() {
        // given
        val nonExistentId = UUID.randomUUID()

        // when & then
        assertThatThrownBy { deleteCommentService.execute(commenterId, nonExistentId) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND)
    }
}
