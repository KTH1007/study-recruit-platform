package com.study.platform.acceptance

import com.study.platform.AbstractAcceptanceTest
import com.study.platform.domain.apply.dto.request.ApplyCreateRequest
import com.study.platform.domain.post.dto.request.StudyPostCreateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import java.util.UUID

class RateLimitAcceptanceTest : AbstractAcceptanceTest() {

    @Test
    fun `게시글 생성 3회 초과 시 429를 반환한다`() {
        val user = createUser()

        // @RateLimit(limit = 3, windowSeconds = 60) 설정
        val statuses = (1..4).map { i ->
            restTemplate.exchange(
                url("/api/posts"), HttpMethod.POST,
                HttpEntity(
                    StudyPostCreateRequest(
                        title = "스터디 $i",
                        description = "내용",
                        techStack = null,
                        maxMembers = 3,
                        deadline = LocalDateTime.now().plusDays(7)
                    ),
                    authHeaders(user).apply {
                        set("Idempotency-Key", UUID.randomUUID().toString())
                    }
                ),
                Map::class.java
            ).statusCode
        }

        assertThat(statuses.take(3)).allMatch { it == HttpStatus.CREATED }
        assertThat(statuses.last()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `다른 사용자의 요청은 Rate Limit에 영향을 주지 않는다`() {
        val user1 = createUser()
        val user2 = createUser()

        // user1이 3회 소진
        repeat(3) { i ->
            restTemplate.exchange(
                url("/api/posts"), HttpMethod.POST,
                HttpEntity(
                    StudyPostCreateRequest("스터디 $i", "내용", null, 3, LocalDateTime.now().plusDays(7)),
                    authHeaders(user1).apply {
                        set("Idempotency-Key", UUID.randomUUID().toString())
                    }
                ),
                Map::class.java
            )
        }

        // user2는 독립적으로 201 반환
        val response = restTemplate.exchange(
            url("/api/posts"), HttpMethod.POST,
            HttpEntity(
                StudyPostCreateRequest("user2 스터디", "내용", null, 3, LocalDateTime.now().plusDays(7)),
                authHeaders(user2).apply {
                    set("Idempotency-Key", UUID.randomUUID().toString())
                }
            ),
            Map::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    }
}
