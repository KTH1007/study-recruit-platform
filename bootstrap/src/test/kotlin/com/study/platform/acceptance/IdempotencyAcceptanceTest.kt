package com.study.platform.acceptance

import com.study.platform.AbstractAcceptanceTest
import com.study.platform.domain.apply.dto.request.ApplyCreateRequest
import com.study.platform.support.TestApplyResponse
import com.study.platform.support.TestPage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.util.UUID

class IdempotencyAcceptanceTest : AbstractAcceptanceTest() {

    @Test
    fun `동일한 Idempotency-Key로 재요청 시 중복 생성되지 않는다`() {
        // given
        val author = createUser()
        val applicant = createUser()
        val postId = createPost(author)
        val idempotencyKey = UUID.randomUUID().toString()
        val headers = authHeaders(applicant).apply {
            set("Idempotency-Key", idempotencyKey)
        }

        // when
        val first = restTemplate.exchange(
            url("/api/posts/$postId/applies"), HttpMethod.POST,
            HttpEntity(ApplyCreateRequest("지원합니다"), headers),
            Map::class.java
        )
        val second = restTemplate.exchange(
            url("/api/posts/$postId/applies"), HttpMethod.POST,
            HttpEntity(ApplyCreateRequest("지원합니다"), headers),
            Map::class.java
        )

        // then
        assertThat(first.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(second.statusCode).isEqualTo(HttpStatus.CREATED)

        val applies = apiExchange<TestPage<TestApplyResponse>>(
            "/api/posts/$postId/applies", HttpMethod.GET,
            HttpEntity<Void>(authHeaders(author))
        )
        val applicantApplies = applies.requireData().content.count { apply ->
            apply.applicantNickname == applicant.nickname
        }
        // 지원 목록에 1건만 존재
        assertThat(applicantApplies).isEqualTo(1)
    }

    @Test
    fun `다른 Idempotency-Key로 요청 시 별도 처리된다`() {
        // given
        val author = createUser()
        val applicant = createUser()
        val postId = createPost(author)

        // when
        val first = restTemplate.exchange(
            url("/api/posts/$postId/applies"), HttpMethod.POST,
            HttpEntity(ApplyCreateRequest("지원합니다"), authHeaders(applicant).apply {
                set("Idempotency-Key", UUID.randomUUID().toString())
            }),
            Map::class.java
        )
        // 이미 지원했으므로 409
        val second = restTemplate.exchange(
            url("/api/posts/$postId/applies"), HttpMethod.POST,
            HttpEntity(ApplyCreateRequest("지원합니다"), authHeaders(applicant).apply {
                set("Idempotency-Key", UUID.randomUUID().toString())
            }),
            Map::class.java
        )

        // then
        assertThat(first.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(second.statusCode).isEqualTo(HttpStatus.CONFLICT)
    }
}
