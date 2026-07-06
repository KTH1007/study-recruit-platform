package com.study.platform.acceptance

import com.study.platform.AbstractAcceptanceTest
import com.study.platform.domain.apply.dto.request.ApplyCreateRequest
import com.study.platform.domain.user.model.User
import com.study.platform.support.TestApplyResponse
import com.study.platform.support.TestPage
import com.study.platform.support.TestTeamResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.util.UUID

class ChatFlowAcceptanceTest : AbstractAcceptanceTest() {

    private fun joinTeam(leader: User, applicant: User, postId: UUID): UUID {
        val applyResponse = apiExchange<TestApplyResponse>(
            "/api/posts/$postId/applies", HttpMethod.POST,
            HttpEntity(ApplyCreateRequest("참여하고 싶습니다"), authHeaders(applicant))
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
    fun `팀원은 채팅 메시지를 조회할 수 있다`() {
        // given
        val leader = createUser()
        val member = createUser()
        val postId = createPost(leader)
        val teamId = joinTeam(leader, member, postId)

        // when
        val response = apiExchange<TestPage<Any>>(
            "/api/teams/$teamId/chat", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(member))
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.requireData().content).isEmpty()
    }

    @Test
    fun `팀원이 아닌 사용자는 채팅을 조회할 수 없다`() {
        // given
        val leader = createUser()
        val member = createUser()
        val outsider = createUser()
        val postId = createPost(leader)
        val teamId = joinTeam(leader, member, postId)

        // when
        val response = restTemplate.exchange(
            url("/api/teams/$teamId/chat"), HttpMethod.GET,
            HttpEntity<Void>(authHeaders(outsider)),
            Map::class.java
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }
}
