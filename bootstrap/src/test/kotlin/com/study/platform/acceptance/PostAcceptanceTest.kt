package com.study.platform.acceptance

import com.study.platform.AbstractAcceptanceTest
import com.study.platform.domain.post.dto.request.StudyPostCreateRequest
import com.study.platform.domain.post.dto.request.StudyPostUpdateRequest
import com.study.platform.support.TestPostResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class PostAcceptanceTest : AbstractAcceptanceTest() {

    @Test
    fun `게시글을 생성하면 201을 반환한다`() {
        // given
        val user = createUser()
        val request = StudyPostCreateRequest(
            title = "Kotlin 스터디 모집",
            description = "주 2회 온라인 진행",
            techStack = "Kotlin,Spring",
            maxMembers = 5,
            deadline = LocalDateTime.now().plusDays(7)
        )

        // when
        val response = apiExchange<TestPostResponse>(
            "/api/posts", HttpMethod.POST,
            HttpEntity(request, authHeaders(user))
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.requireData().title).isEqualTo("Kotlin 스터디 모집")
    }

    @Test
    fun `게시글을 수정하면 변경된 내용이 반환된다`() {
        // given
        val user = createUser()
        val createRequest = StudyPostCreateRequest(
            title = "수정 전 제목",
            description = "내용",
            techStack = null,
            maxMembers = 4,
            deadline = LocalDateTime.now().plusDays(7)
        )
        val postId = apiExchange<TestPostResponse>(
            "/api/posts", HttpMethod.POST,
            HttpEntity(createRequest, authHeaders(user))
        ).requireData().id
        val updateRequest = StudyPostUpdateRequest(
            title = "수정 후 제목",
            description = "수정된 내용",
            techStack = "Java",
            maxMembers = 6,
            deadline = LocalDateTime.now().plusDays(14)
        )

        // when
        val response = apiExchange<TestPostResponse>(
            "/api/posts/$postId", HttpMethod.PATCH,
            HttpEntity(updateRequest, authHeaders(user))
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.requireData().title).isEqualTo("수정 후 제목")
    }

    @Test
    fun `게시글을 삭제하면 조회 시 404를 반환한다`() {
        // given
        val user = createUser()
        val postId = createPost(user, "삭제할 게시글")

        // when
        restTemplate.exchange(
            url("/api/posts/$postId"), HttpMethod.DELETE,
            HttpEntity<Void>(authHeaders(user)),
            Map::class.java
        )
        val response = restTemplate.exchange(
            url("/api/posts/$postId"), HttpMethod.GET,
            HttpEntity<Void>(authHeaders(user)),
            Map::class.java
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `다른 사람의 게시글을 삭제하면 403을 반환한다`() {
        // given
        val owner = createUser()
        val other = createUser()
        val postId = createPost(owner, "내 게시글")

        // when
        val response = restTemplate.exchange(
            url("/api/posts/$postId"), HttpMethod.DELETE,
            HttpEntity<Void>(authHeaders(other)),
            Map::class.java
        )

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }
}