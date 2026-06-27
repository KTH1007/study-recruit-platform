package com.study.platform.acceptance

import com.study.platform.AbstractAcceptanceTest
import com.study.platform.domain.post.dto.request.StudyPostCreateRequest
import com.study.platform.domain.post.dto.request.StudyPostUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class PostAcceptanceTest : AbstractAcceptanceTest() {

    @Test
    fun `게시글을 생성하면 201을 반환한다`() {
        val user = createUser()
        val request = StudyPostCreateRequest(
            title = "Kotlin 스터디 모집",
            description = "주 2회 온라인 진행",
            techStack = "Kotlin,Spring",
            maxMembers = 5,
            deadline = LocalDateTime.now().plusDays(7)
        )

        val response = restTemplate.exchange(
            url("/api/posts"), HttpMethod.POST,
            HttpEntity(request, authHeaders(user)),
            Map::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        val data = response.body!!["data"] as Map<*, *>
        assertThat(data["title"]).isEqualTo("Kotlin 스터디 모집")
    }

    @Test
    fun `게시글을 수정하면 변경된 내용이 반환된다`() {
        val user = createUser()
        val createRequest = StudyPostCreateRequest(
            title = "수정 전 제목",
            description = "내용",
            techStack = null,
            maxMembers = 4,
            deadline = LocalDateTime.now().plusDays(7)
        )
        val postId = (restTemplate.exchange(
            url("/api/posts"), HttpMethod.POST,
            HttpEntity(createRequest, authHeaders(user)),
            Map::class.java
        ).body!!["data"] as Map<*, *>)["id"]

        val updateRequest = StudyPostUpdateRequest(
            title = "수정 후 제목",
            description = "수정된 내용",
            techStack = "Java",
            maxMembers = 6,
            deadline = LocalDateTime.now().plusDays(14)
        )

        val response = restTemplate.exchange(
            url("/api/posts/$postId"), HttpMethod.PATCH,
            HttpEntity(updateRequest, authHeaders(user)),
            Map::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val data = response.body!!["data"] as Map<*, *>
        assertThat(data["title"]).isEqualTo("수정 후 제목")
    }

    @Test
    fun `게시글을 삭제하면 조회 시 404를 반환한다`() {
        val user = createUser()
        val createRequest = StudyPostCreateRequest(
            title = "삭제할 게시글",
            description = "내용",
            techStack = null,
            maxMembers = 3,
            deadline = LocalDateTime.now().plusDays(7)
        )
        val postId = (restTemplate.exchange(
            url("/api/posts"), HttpMethod.POST,
            HttpEntity(createRequest, authHeaders(user)),
            Map::class.java
        ).body!!["data"] as Map<*, *>)["id"]

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
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `다른 사람의 게시글을 삭제하면 403을 반환한다`() {
        val owner = createUser()
        val other = createUser()
        val createRequest = StudyPostCreateRequest(
            title = "내 게시글",
            description = "내용",
            techStack = null,
            maxMembers = 3,
            deadline = LocalDateTime.now().plusDays(7)
        )
        val postId = (restTemplate.exchange(
            url("/api/posts"), HttpMethod.POST,
            HttpEntity(createRequest, authHeaders(owner)),
            Map::class.java
        ).body!!["data"] as Map<*, *>)["id"]

        val response = restTemplate.exchange(
            url("/api/posts/$postId"), HttpMethod.DELETE,
            HttpEntity<Void>(authHeaders(other)),
            Map::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }
}