package com.study.platform.domain.team.model

import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.user.model.User
import com.study.platform.domain.user.model.UserRepository
import com.study.platform.global.support.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Transactional
class StudyTeamRepositoryTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var studyTeamRepository: StudyTeamRepository

    @Autowired
    private lateinit var studyPostRepository: StudyPostRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var post: StudyPost
    private lateinit var team: StudyTeam

    @BeforeEach
    fun setUp() {
        val author = userRepository.save(User.create("kakao-1", "작성자", "author@test.com"))
        post = studyPostRepository.save(
            StudyPost.create(author, "스터디 모집", "열심히 합니다", "Java", 5, LocalDateTime.now().plusDays(7))
        )
        team = studyTeamRepository.save(StudyTeam.create(post))
    }

    @Test
    fun `findByPostId_성공`() {
        val result: StudyTeam? = studyTeamRepository.findByPostId(post.id!!)

        assertThat(result).isNotNull()
        assertThat(result!!.name).isEqualTo("스터디 모집")
    }

    @Test
    fun `findByPostId_존재하지않음_빈Optional`() {
        val result: StudyTeam? = studyTeamRepository.findByPostId(UUID.randomUUID())

        assertThat(result).isNull()
    }

    @Test
    fun `save_팀생성_성공`() {
        assertThat(team.id).isNotNull()
        assertThat(team.name).isEqualTo(post.title)
        assertThat(team.post?.id).isEqualTo(post.id)
    }
}
