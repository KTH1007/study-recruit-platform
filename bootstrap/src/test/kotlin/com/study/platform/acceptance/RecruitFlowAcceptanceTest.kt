package com.study.platform.acceptance

import com.study.platform.AbstractAcceptanceTest
import com.study.platform.domain.apply.dto.request.ApplyCreateRequest
import com.study.platform.support.TestApplyResponse
import com.study.platform.support.TestPage
import com.study.platform.support.TestTeamMemberResponse
import com.study.platform.support.TestTeamResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class RecruitFlowAcceptanceTest : AbstractAcceptanceTest() {

    @Test
    fun `게시글 작성 후 지원하면 지원 목록에 조회된다`() {
        // given
        val leader = createUser()
        val applicant = createUser()
        val postId = createPost(leader, "스터디 모집")
        apiExchange<TestApplyResponse>(
            "/api/posts/$postId/applies", HttpMethod.POST,
            HttpEntity(ApplyCreateRequest("지원합니다"), authHeaders(applicant))
        )

        // when
        val response = apiExchange<TestPage<TestApplyResponse>>(
            "/api/posts/$postId/applies", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(leader))
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.requireData().content).hasSize(1)
    }

    @Test
    fun `지원 승인 후 팀원 목록에 포함된다`() {
        // given
        val leader = createUser()
        val applicant = createUser()
        val postId = createPost(leader, "스터디 모집")
        val applyResponse = apiExchange<TestApplyResponse>(
            "/api/posts/$postId/applies", HttpMethod.POST,
            HttpEntity(ApplyCreateRequest("지원합니다"), authHeaders(applicant))
        )
        val applyId = applyResponse.requireData().id

        // when
        apiExchange<TestApplyResponse>(
            "/api/applies/$applyId/approve", HttpMethod.PATCH,
            HttpEntity<Void>(authHeaders(leader))
        )
        val teamResponse = apiExchange<TestTeamResponse>(
            "/api/posts/$postId/team", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(leader))
        )
        val teamId = teamResponse.requireData().id
        val membersResponse = apiExchange<List<TestTeamMemberResponse>>(
            "/api/teams/$teamId/members", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(leader))
        )

        // then
        val nicknames = membersResponse.requireData().map { it.nickname }
        assertThat(nicknames).contains(applicant.nickname)
    }

    @Test
    fun `지원 거절 후 지원 상태가 REJECTED가 된다`() {
        // given
        val leader = createUser()
        val applicant = createUser()
        val postId = createPost(leader, "스터디 모집")
        val applyResponse = apiExchange<TestApplyResponse>(
            "/api/posts/$postId/applies", HttpMethod.POST,
            HttpEntity(ApplyCreateRequest("지원합니다"), authHeaders(applicant))
        )
        val applyId = applyResponse.requireData().id

        // when
        apiExchange<TestApplyResponse>(
            "/api/applies/$applyId/reject", HttpMethod.PATCH,
            HttpEntity<Void>(authHeaders(leader))
        )
        val applies = apiExchange<TestPage<TestApplyResponse>>(
            "/api/posts/$postId/applies", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(leader))
        )

        // then
        assertThat(applies.requireData().content[0].status).isEqualTo("REJECTED")
    }
}
