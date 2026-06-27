package com.study.platform.acceptance

import com.study.platform.AbstractAcceptanceTest
import com.study.platform.domain.apply.dto.request.ApplyCreateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class RecruitFlowAcceptanceTest : AbstractAcceptanceTest() {

    @Test
    fun `게시글 작성 후 지원하면 지원 목록에 조회된다`() {
        val leader = createUser()
        val applicant = createUser()
        val postId = createPost(leader, "스터디 모집")

        restTemplate.exchange(
            url("/api/posts/$postId/applies"), HttpMethod.POST,
            HttpEntity(ApplyCreateRequest("지원합니다"), authHeaders(applicant)),
            Map::class.java
        )

        val response = restTemplate.exchange(
            url("/api/posts/$postId/applies"), HttpMethod.GET,
            HttpEntity<Void>(authHeaders(leader)),
            Map::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val applies = response.body!!["data"] as List<*>
        assertThat(applies).hasSize(1)
    }

    @Test
    fun `지원 승인 후 팀원 목록에 포함된다`() {
        val leader = createUser()
        val applicant = createUser()
        val postId = createPost(leader, "스터디 모집")

        // 지원
        val applyResponse = restTemplate.exchange(
            url("/api/posts/$postId/applies"), HttpMethod.POST,
            HttpEntity(ApplyCreateRequest("지원합니다"), authHeaders(applicant)),
            Map::class.java
        )
        val applyId = (applyResponse.body!!["data"] as Map<*, *>)["id"]

        // 승인
        restTemplate.exchange(
            url("/api/applies/$applyId/approve"), HttpMethod.PATCH,
            HttpEntity<Void>(authHeaders(leader)),
            Map::class.java
        )

        // 팀 조회
        val teamResponse = restTemplate.exchange(
            url("/api/posts/$postId/team"), HttpMethod.GET,
            HttpEntity<Void>(authHeaders(leader)),
            Map::class.java
        )
        val teamId = (teamResponse.body!!["data"] as Map<*, *>)["id"]

        // 팀원 목록
        val membersResponse = restTemplate.exchange(
            url("/api/teams/$teamId/members"), HttpMethod.GET,
            HttpEntity<Void>(authHeaders(leader)),
            Map::class.java
        )

        val members = membersResponse.body!!["data"] as List<*>
        val nicknames = members.map { ((it as Map<*, *>)["nickname"]) }
        assertThat(nicknames).contains(applicant.nickname)
    }

    @Test
    fun `지원 거절 후 지원 상태가 REJECTED가 된다`() {
        val leader = createUser()
        val applicant = createUser()
        val postId = createPost(leader, "스터디 모집")

        val applyResponse = restTemplate.exchange(
            url("/api/posts/$postId/applies"), HttpMethod.POST,
            HttpEntity(ApplyCreateRequest("지원합니다"), authHeaders(applicant)),
            Map::class.java
        )
        val applyId = (applyResponse.body!!["data"] as Map<*, *>)["id"]

        restTemplate.exchange(
            url("/api/applies/$applyId/reject"), HttpMethod.PATCH,
            HttpEntity<Void>(authHeaders(leader)),
            Map::class.java
        )

        val applies = restTemplate.exchange(
            url("/api/posts/$postId/applies"), HttpMethod.GET,
            HttpEntity<Void>(authHeaders(leader)),
            Map::class.java
        )
        val status = ((applies.body!!["data"] as List<*>)[0] as Map<*, *>)["status"]
        assertThat(status).isEqualTo("REJECTED")
    }
}
