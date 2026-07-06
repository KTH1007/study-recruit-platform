package com.study.platform.domain.post.model

import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse
import com.study.platform.domain.post.port.StudyPostQueryPort
import com.study.platform.domain.user.model.User
import com.study.platform.domain.user.model.UserRepository
import com.study.platform.global.support.AbstractIntegrationTest
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
class StudyPostRepositoryTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var studyPostRepository: StudyPostRepository

    @Autowired
    private lateinit var studyPostQueryPort: StudyPostQueryPort

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var author: User
    private lateinit var post: StudyPost

    @BeforeEach
    fun setUp() {
        author = userRepository.save(User.create("kakao-1", "작성자", "author@test.com"))
        post = studyPostRepository.save(
            StudyPost.create(author, "스터디 모집", "열심히 합니다", "Java", 5, LocalDateTime.now().plusDays(7))
        )
    }

    @Test
    fun `findByIdWithAuthor_성공`() {
        val result: StudyPost? = studyPostRepository.findByIdWithAuthor(post.id!!)

        assertThat(result).isNotNull()
        assertThat(result!!.title).isEqualTo("스터디 모집")
        assertThat(result.author?.nickname).isEqualTo("작성자")
    }

    @Test
    fun `findByIdWithAuthor_존재하지않음_빈Optional`() {
        val result: StudyPost? = studyPostRepository.findByIdWithAuthor(UUID.randomUUID())

        assertThat(result).isNull()
    }

    @Test
    fun `findByIdWithAuthorForUpdate_성공`() {
        val result: StudyPost? = studyPostRepository.findByIdWithAuthorForUpdate(post.id!!)

        assertThat(result).isNotNull()
        assertThat(result!!.id).isEqualTo(post.id)
    }

    @Test
    fun `findAllWithFilter_techStack_필터링`() {
        studyPostRepository.save(
            StudyPost.create(author, "Kotlin 스터디", "열심히 합니다", "Kotlin", 3, LocalDateTime.now().plusDays(7))
        )

        val result: Page<StudyPostSummaryResponse> = studyPostQueryPort.findAllWithFilter(
            "Java", null, PageRequest.of(0, 10)
        )

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].techStack).isEqualTo("Java")
    }

    @Test
    fun `findAllWithFilter_status_필터링`() {
        val closedPost = studyPostRepository.save(
            StudyPost.create(author, "마감된 스터디", "열심히 합니다", "Python", 3, LocalDateTime.now().plusDays(7))
        )
        closedPost.close()
        studyPostRepository.save(closedPost)

        val result: Page<StudyPostSummaryResponse> = studyPostQueryPort.findAllWithFilter(
            null, StudyPostStatus.OPEN, PageRequest.of(0, 10)
        )

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].status).isEqualTo(StudyPostStatus.OPEN)
    }

    @Test
    fun `findAllWithFilter_필터없음_전체조회`() {
        studyPostRepository.save(
            StudyPost.create(author, "두 번째 스터디", "열심히 합니다", "Kotlin", 3, LocalDateTime.now().plusDays(7))
        )

        val result: Page<StudyPostSummaryResponse> = studyPostQueryPort.findAllWithFilter(
            null, null, PageRequest.of(0, 10)
        )

        assertThat(result.totalElements).isEqualTo(2)
    }

    @Test
    fun `findDeadlineReminderPosts_마감임박게시글_조회`() {
        val tomorrow = LocalDateTime.now().plusDays(1)
        studyPostRepository.save(
            StudyPost.create(author, "마감 임박 스터디", "열심히 합니다", "Java", 3, tomorrow)
        )

        val result: List<StudyPost> = studyPostRepository.findDeadlineReminderPosts(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(2),
            StudyPostStatus.OPEN
        )

        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("마감 임박 스터디")
    }
}
