package com.study.platform.acceptance

import com.study.platform.AbstractAcceptanceTest
import com.study.platform.domain.apply.dto.request.ApplyCreateRequest
import com.study.platform.domain.team.dto.request.TeamScheduleCreateRequest
import com.study.platform.domain.user.model.User
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.outbox.infrastructure.OutboxEventJpaRepository
import com.study.platform.support.TestApplyResponse
import com.study.platform.support.TestTeamResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import java.util.UUID

class ScheduleNotificationAcceptanceTest : AbstractAcceptanceTest() {

    @Autowired
    private lateinit var outboxEventJpaRepository: OutboxEventJpaRepository

    private fun joinTeam(leader: User, member: User, postId: UUID): UUID {
        val applyResponse = apiExchange<TestApplyResponse>(
            "/api/posts/$postId/applies", HttpMethod.POST,
            HttpEntity(ApplyCreateRequest("참여하고 싶습니다"), authHeaders(member))
        )
        val applyId = applyResponse.requireData().id
        apiExchange<TestApplyResponse>(
            "/api/applies/$applyId/approve", HttpMethod.PATCH,
            HttpEntity<Void>(authHeaders(leader))
        )
        val teamResponse = apiExchange<TestTeamResponse>(
            "/api/posts/$postId/team", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(leader))
        )
        return teamResponse.requireData().id
    }

    @Test
    fun `일정 생성 시 팀원 수만큼 Outbox 이벤트가 저장된다`() {
        // given
        val leader = createUser()
        val member = createUser()
        val postId = createPost(leader)
        val teamId = joinTeam(leader, member, postId)
        val snapshot = LocalDateTime.now()
        val request = TeamScheduleCreateRequest(
            title = "1회차 스터디",
            description = "온라인 미팅",
            scheduledAt = LocalDateTime.now().plusDays(3)
        )

        // when
        val response = restTemplate.exchange(
            url("/api/teams/$teamId/schedules"), HttpMethod.POST,
            HttpEntity(request, authHeaders(leader)),
            Map::class.java
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        val notificationEvents = outboxEventJpaRepository.findAll()
            .filter { it.topic == KafkaConstants.NOTIFICATION_TOPIC && it.createdAt?.isAfter(snapshot) == true }
        // 팀원 2명(리더 + 멤버)에게 알림 이벤트 저장
        assertThat(notificationEvents).hasSize(2)
    }

    @Test
    fun `팀원이 아닌 사용자는 일정을 생성할 수 없다`() {
        // given
        val leader = createUser()
        val member = createUser()
        val outsider = createUser()
        val postId = createPost(leader)
        val teamId = joinTeam(leader, member, postId)
        val request = TeamScheduleCreateRequest(
            title = "무단 일정",
            description = null,
            scheduledAt = LocalDateTime.now().plusDays(1)
        )

        // when
        val response = restTemplate.exchange(
            url("/api/teams/$teamId/schedules"), HttpMethod.POST,
            HttpEntity(request, authHeaders(outsider)),
            Map::class.java
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }
}
