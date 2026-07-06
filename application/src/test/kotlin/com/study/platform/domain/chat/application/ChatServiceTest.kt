package com.study.platform.domain.chat.application

import com.study.platform.domain.chat.dto.request.ChatMessageRequest
import com.study.platform.domain.chat.model.ChatMessage
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.team.model.StudyTeam
import com.study.platform.domain.user.model.User
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.support.TestFixtures
import com.study.platform.support.fake.FakeChatMessageRepository
import com.study.platform.support.fake.FakeChatPublisher
import com.study.platform.support.fake.FakeChatQueryPort
import com.study.platform.support.fake.FakeStudyTeamRepository
import com.study.platform.support.fake.FakeTeamMemberRepository
import com.study.platform.support.fake.FakeUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import java.util.UUID

class ChatServiceTest {

    private lateinit var chatMessageRepository: FakeChatMessageRepository
    private lateinit var studyTeamRepository: FakeStudyTeamRepository
    private lateinit var teamMemberRepository: FakeTeamMemberRepository
    private lateinit var userRepository: FakeUserRepository
    private lateinit var chatPublisher: FakeChatPublisher

    private lateinit var findChatMessagesService: FindChatMessagesService
    private lateinit var saveAndPublishChatMessageService: SaveAndPublishChatMessageService

    private lateinit var leaderId: UUID
    private lateinit var teamId: UUID
    private lateinit var leader: User
    private lateinit var post: StudyPost
    private lateinit var team: StudyTeam

    @BeforeEach
    fun setUp() {
        chatMessageRepository = FakeChatMessageRepository()
        studyTeamRepository = FakeStudyTeamRepository()
        teamMemberRepository = FakeTeamMemberRepository()
        userRepository = FakeUserRepository()
        chatPublisher = FakeChatPublisher()

        findChatMessagesService = FindChatMessagesService(FakeChatQueryPort(chatMessageRepository), teamMemberRepository)
        saveAndPublishChatMessageService = SaveAndPublishChatMessageService(
            chatMessageRepository, studyTeamRepository, teamMemberRepository, userRepository, chatPublisher
        )

        leaderId = UUID.randomUUID()
        leader = TestFixtures.createUser(id = leaderId, kakaoId = "kakao-1", nickname = "팀장", email = "leader@test.com")
        post = TestFixtures.createStudyPost(author = leader)
        team = TestFixtures.createStudyTeam(post = post)
        teamId = checkNotNull(team.id)

        userRepository.save(leader)
        studyTeamRepository.save(team)
        teamMemberRepository.save(TestFixtures.createLeaderMember(team = team, user = leader))
    }

    @Test
    fun `findMessages_팀원인_경우_메시지_조회`() {
        // given
        chatMessageRepository.save(ChatMessage.create(team, leader, "안녕하세요"))

        // when
        val result = findChatMessagesService.execute(leaderId, teamId, PageRequest.of(0, 10))

        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].content).isEqualTo("안녕하세요")
    }

    @Test
    fun `findMessages_팀원_아닌_경우_예외발생`() {
        // given
        val outsiderId = UUID.randomUUID()

        // when & then
        assertThatThrownBy { findChatMessagesService.execute(outsiderId, teamId, PageRequest.of(0, 10)) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_TEAM_MEMBER)
    }

    @Test
    fun `sendMessage_정상_저장_및_발행`() {
        // given
        val request = ChatMessageRequest("안녕하세요")

        // when
        saveAndPublishChatMessageService.execute(leaderId, teamId, request)

        // then
        assertThat(chatMessageRepository.findAllByTeamId(teamId)).hasSize(1)
        assertThat(chatPublisher.getPublished()).hasSize(1)
        assertThat(chatPublisher.getPublished()[0].content).isEqualTo("안녕하세요")
    }

    @Test
    fun `sendMessage_팀원_아닌_경우_예외발생`() {
        // given
        val outsiderId = UUID.randomUUID()
        val request = ChatMessageRequest("안녕하세요")

        // when & then
        assertThatThrownBy { saveAndPublishChatMessageService.execute(outsiderId, teamId, request) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_TEAM_MEMBER)
        assertThat(chatPublisher.getPublished()).isEmpty()
    }

    @Test
    fun `sendMessage_팀_존재하지않음_예외발생`() {
        // given
        val orphanTeamId = UUID.randomUUID()
        teamMemberRepository.save(TestFixtures.createLeaderMember(team = TestFixtures.createStudyTeam(id = orphanTeamId), user = leader))
        val request = ChatMessageRequest("안녕하세요")

        // when & then
        assertThatThrownBy { saveAndPublishChatMessageService.execute(leaderId, orphanTeamId, request) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEAM_NOT_FOUND)
    }

    @Test
    fun `sendMessage_유저_존재하지않음_예외발생`() {
        // given
        val ghostUserId = UUID.randomUUID()
        teamMemberRepository.save(TestFixtures.createNormalMember(team = team, user = TestFixtures.createUser(id = ghostUserId)))
        val request = ChatMessageRequest("안녕하세요")

        // when & then
        assertThatThrownBy { saveAndPublishChatMessageService.execute(ghostUserId, teamId, request) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND)
    }
}
