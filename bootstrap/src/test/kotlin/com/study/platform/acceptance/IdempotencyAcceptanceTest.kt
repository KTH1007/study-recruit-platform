package com.study.platform.acceptance

import com.study.platform.AbstractAcceptanceTest
import com.study.platform.domain.apply.dto.request.ApplyCreateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.util.UUID

class IdempotencyAcceptanceTest : AbstractAcceptanceTest() {

    @Test
    fun `동일한 Idempotency-Key로 재요청 시 중복 생성되지 않는다`() {
        val author = createUser()
        val applicant = createUser()
        val postId = createPost(author)
        val idempotencyKey = UUID.randomUUID().toString()

        val headers = authHeaders(applicant).apply {
            set("Idempotency-Key", idempotencyKey)
        }

        // 첫 번째 요청
        val first = restTemplate.exchange(
            url("/api/posts/$postId/applies"), HttpMethod.POST,
            HttpEntity(ApplyCreateRequest("지원합니다"), headers),
            Map::class.java
        )
        assertThat(first.statusCode).isEqualTo(HttpStatus.CREATED)

        // 동일 키로 재요청
        val second = restTemplate.exchange(
            url("/api/posts/$postId/applies"), HttpMethod.POST,
            HttpEntity(ApplyCreateRequest("지원합니다"), headers),
            Map::class.java
        )
        assertThat(second.statusCode).isEqualTo(HttpStatus.CREATED)

        // 지원 목록에 1건만 존재
        val applies = restTemplate.exchange(
            url("/api/posts/$postId/applies"), HttpMethod.GET,
            HttpEntity<Void>(authHeaders(author)),
            Map::class.java
        )
        val applyList = applies.body!!["data"] as List<*>
        val applicantApplies = applyList.count { apply ->
            ((apply as Map<*, *>)["applicantNickname"]) == applicant.nickname
        }
        assertThat(applicantApplies).isEqualTo(1)
    }

    @Test
    fun `다른 Idempotency-Key로 요청 시 별도 처리된다`() {
        val author = createUser()
        val applicant = createUser()
        val postId = createPost(author)

        // 첫 번째 요청 (key1)
        val first = restTemplate.exchange(
            url("/api/posts/$postId/applies"), HttpMethod.POST,
            HttpEntity(ApplyCreateRequest("지원합니다"), authHeaders(applicant).apply {
                set("Idempotency-Key", UUID.randomUUID().toString())
            }),
            Map::class.java
        )
        assertThat(first.statusCode).isEqualTo(HttpStatus.CREATED)

        // 두 번째 요청 (key2) - 이미 지원했으므로 409
        val second = restTemplate.exchange(
            url("/api/posts/$postId/applies"), HttpMethod.POST,
            HttpEntity(ApplyCreateRequest("지원합니다"), authHeaders(applicant).apply {
                set("Idempotency-Key", UUID.randomUUID().toString())
            }),
            Map::class.java
        )
        assertThat(second.statusCode).isEqualTo(HttpStatus.CONFLICT)
    }
}
