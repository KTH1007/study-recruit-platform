package com.study.platform.acceptance

import com.study.platform.AbstractAcceptanceTest
import com.study.platform.domain.apply.dto.request.ApplyCreateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class ChatFlowAcceptanceTest : AbstractAcceptanceTest() {

    private fun joinTeam(leader: com.study.platform.domain.user.model.User,
                         applicant: com.study.platform.domain.user.model.User,
                         postId: Any?): Any? {
        val applyResponse = restTemplate.exchange(
            url("/api/posts/$postId/applies"), HttpMethod.POST,
            HttpEntity(ApplyCreateRequest("참여하고 싶습니다"), authHeaders(applicant)),
            Map::class.java
        )
        val applyId = (applyResponse.body!!["data"] as Map<*, *>)["id"]

        restTemplate.exchange(
            url("/api/applies/$applyId/approve"), HttpMethod.PATCH,
            HttpEntity<Void>(authHeaders(leader)),
            Map::class.java
        )

        val teamResponse = restTemplate.exchange(
            url("/api/posts/$postId/team"), HttpMethod.GET,
            HttpEntity<Void>(authHeaders(leader)),
            Map::class.java
        )
        return (teamResponse.body!!["data"] as Map<*, *>)["id"]
    }

    @Test
    fun `팀원은 채팅 메시지를 조회할 수 있다`() {
        val leader = createUser()
        val member = createUser()
        val postId = createPost(leader)
        val teamId = joinTeam(leader, member, postId)

        val response = restTemplate.exchange(
            url("/api/teams/$teamId/chat"), HttpMethod.GET,
            HttpEntity<Void>(authHeaders(member)),
            Map::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val data = response.body!!["data"] as Map<*, *>
        assertThat(data["content"] as List<*>).isEmpty()
    }

    @Test
    fun `팀원이 아닌 사용자는 채팅을 조회할 수 없다`() {
        val leader = createUser()
        val member = createUser()
        val outsider = createUser()
        val postId = createPost(leader)
        val teamId = joinTeam(leader, member, postId)

        val response = restTemplate.exchange(
            url("/api/teams/$teamId/chat"), HttpMethod.GET,
            HttpEntity<Void>(authHeaders(outsider)),
            Map::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }
}
