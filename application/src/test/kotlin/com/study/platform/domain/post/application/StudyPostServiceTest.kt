package com.study.platform.domain.post.application

import com.study.platform.domain.post.dto.request.StudyPostCreateRequest
import com.study.platform.domain.post.dto.request.StudyPostUpdateRequest
import com.study.platform.domain.post.dto.response.StudyPostResponse
import com.study.platform.domain.post.event.PostSyncEvent
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.post.model.StudyPostStatus
import com.study.platform.domain.user.model.User
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.global.outbox.application.OutboxEventService
import com.study.platform.support.TestFixtures
import com.study.platform.support.fake.FakeApplyRepository
import com.study.platform.support.fake.FakeCommentRepository
import com.study.platform.support.fake.FakeDomainEventPublisher
import com.study.platform.support.fake.FakeOutboxEventRepository
import com.study.platform.support.fake.FakeStudyPostQueryPort
import com.study.platform.support.fake.FakeStudyPostRepository
import com.study.platform.support.fake.FakeStudyTeamRepository
import com.study.platform.support.fake.FakeTeamMemberRepository
import com.study.platform.support.fake.FakeTeamScheduleRepository
import com.study.platform.support.fake.FakeUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import java.util.UUID

class StudyPostServiceTest {

    private lateinit var studyPostRepository: FakeStudyPostRepository
    private lateinit var userRepository: FakeUserRepository
    private lateinit var studyTeamRepository: FakeStudyTeamRepository
    private lateinit var teamMemberRepository: FakeTeamMemberRepository
    private lateinit var teamScheduleRepository: FakeTeamScheduleRepository
    private lateinit var commentRepository: FakeCommentRepository
    private lateinit var applyRepository: FakeApplyRepository
    private lateinit var eventPublisher: FakeDomainEventPublisher
    private lateinit var outboxEventService: OutboxEventService

    private lateinit var findStudyPostService: FindStudyPostService
    private lateinit var createStudyPostService: CreateStudyPostService
    private lateinit var updateStudyPostService: UpdateStudyPostService
    private lateinit var deleteStudyPostService: DeleteStudyPostService
    private lateinit var closeStudyPostService: CloseStudyPostService

    private lateinit var authorId: UUID
    private lateinit var postId: UUID
    private lateinit var author: User
    private lateinit var post: StudyPost

    @BeforeEach
    fun setUp() {
        studyPostRepository = FakeStudyPostRepository()
        userRepository = FakeUserRepository()
        studyTeamRepository = FakeStudyTeamRepository()
        teamMemberRepository = FakeTeamMemberRepository()
        teamScheduleRepository = FakeTeamScheduleRepository()
        commentRepository = FakeCommentRepository()
        applyRepository = FakeApplyRepository()
        eventPublisher = FakeDomainEventPublisher()
        outboxEventService = OutboxEventService(FakeOutboxEventRepository())
        val objectMapper = ObjectMapper()

        findStudyPostService = FindStudyPostService(studyPostRepository)
        createStudyPostService = CreateStudyPostService(studyPostRepository, userRepository, eventPublisher, outboxEventService, objectMapper)
        updateStudyPostService = UpdateStudyPostService(studyPostRepository, eventPublisher, outboxEventService, objectMapper)
        deleteStudyPostService = DeleteStudyPostService(
            studyPostRepository, studyTeamRepository, teamMemberRepository, teamScheduleRepository,
            commentRepository, applyRepository, eventPublisher, outboxEventService, objectMapper
        )
        closeStudyPostService = CloseStudyPostService(studyPostRepository, eventPublisher, outboxEventService, objectMapper)

        authorId = UUID.randomUUID()
        postId = UUID.randomUUID()

        author = TestFixtures.createUser(id = authorId, kakaoId = "kakao1", nickname = "작성자", email = "author@test.com")
        post = TestFixtures.createStudyPost(id = postId, author = author, techStack = "Java", maxMembers = 3)

        userRepository.save(author)
        studyPostRepository.save(post)
    }

    @Test
    fun `findPost_성공`() {
        // given
        // post is already saved in setUp

        // when
        val response: StudyPostResponse = findStudyPostService.execute(postId)

        // then
        assertThat(response.id).isEqualTo(postId)
        assertThat(response.title).isEqualTo("스터디 모집")
    }

    @Test
    fun `findPost_게시글없음_예외발생`() {
        // given
        val nonExistentId = UUID.randomUUID()

        // when & then
        assertThatThrownBy { findStudyPostService.execute(nonExistentId) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND)
    }

    @Test
    fun `createPost_성공`() {
        // given
        val request = StudyPostCreateRequest(
            "새 스터디", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7)
        )

        // when
        val response: StudyPostResponse = createStudyPostService.execute(authorId, request)

        // then
        assertThat(response.title).isEqualTo("새 스터디")
        assertThat(eventPublisher.hasEventOf(PostSyncEvent::class.java)).isTrue()
    }

    @Test
    fun `createPost_사용자없음_예외발생`() {
        // given
        val request = StudyPostCreateRequest(
            "새 스터디", "열심히 합니다", "Java", 3, LocalDateTime.now().plusDays(7)
        )

        // when & then
        assertThatThrownBy { createStudyPostService.execute(UUID.randomUUID(), request) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND)
    }

    @Test
    fun `updatePost_성공`() {
        // given
        val request = StudyPostUpdateRequest(
            "수정된 제목", "수정된 내용", "Kotlin", 5, LocalDateTime.now().plusDays(14)
        )

        // when
        val response: StudyPostResponse = updateStudyPostService.execute(authorId, postId, request)

        // then
        assertThat(response.title).isEqualTo("수정된 제목")
        assertThat(eventPublisher.hasEventOf(PostSyncEvent::class.java)).isTrue()
    }

    @Test
    fun `updatePost_작성자아님_예외발생`() {
        // given
        val request = StudyPostUpdateRequest(
            "수정된 제목", "수정된 내용", "Kotlin", 5, LocalDateTime.now().plusDays(14)
        )

        // when & then
        assertThatThrownBy { updateStudyPostService.execute(UUID.randomUUID(), postId, request) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN)
    }

    @Test
    fun `deletePost_성공`() {
        // given
        // post is already saved in setUp

        // when
        deleteStudyPostService.execute(authorId, postId)

        // then
        assertThat(studyPostRepository.findById(postId)).isNull()
        assertThat(eventPublisher.hasEventOf(PostSyncEvent::class.java)).isTrue()
    }

    @Test
    fun `deletePost_연관된_팀_댓글_지원서도_함께_삭제된다`() {
        // given
        val team = TestFixtures.createStudyTeam(post = post)
        studyTeamRepository.save(team)
        teamMemberRepository.save(TestFixtures.createLeaderMember(team = team, user = author))
        teamScheduleRepository.save(TestFixtures.createTeamSchedule(team = team))
        commentRepository.save(TestFixtures.createComment(post = post, author = author))
        applyRepository.save(TestFixtures.createApply(post = post, applicant = author))

        // when
        deleteStudyPostService.execute(authorId, postId)

        // then
        assertThat(studyPostRepository.findById(postId)).isNull()
        assertThat(studyTeamRepository.findByPostId(postId)).isNull()
        assertThat(teamMemberRepository.findAllByTeamId(checkNotNull(team.id))).isEmpty()
        assertThat(teamScheduleRepository.findAllByTeamId(checkNotNull(team.id))).isEmpty()
        assertThat(commentRepository.findAllByPostId(postId)).isEmpty()
        assertThat(applyRepository.findAllByPostId(postId)).isEmpty()
    }

    @Test
    fun `deletePost_작성자아님_예외발생`() {
        // given
        val otherId = UUID.randomUUID()

        // when & then
        assertThatThrownBy { deleteStudyPostService.execute(otherId, postId) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN)
    }

    @Test
    fun `closePost_성공`() {
        // given
        // post is already saved in setUp

        // when
        val response: StudyPostResponse = closeStudyPostService.execute(authorId, postId)

        // then
        assertThat(response.status).isEqualTo(StudyPostStatus.CLOSED)
        assertThat(eventPublisher.hasEventOf(PostSyncEvent::class.java)).isTrue()
    }

    @Test
    fun `closePost_작성자아님_예외발생`() {
        // given
        val otherId = UUID.randomUUID()

        // when & then
        assertThatThrownBy { closeStudyPostService.execute(otherId, postId) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN)
    }
}
