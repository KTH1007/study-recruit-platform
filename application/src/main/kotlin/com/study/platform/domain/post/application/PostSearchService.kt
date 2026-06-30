package com.study.platform.domain.post.application

import com.study.platform.domain.post.model.PostSearchPort
import com.study.platform.domain.post.model.PostSearchResult
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.post.model.StudyPostStatus
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PostSearchService(
    private val postSearchPort: PostSearchPort
) {
    private val log = LoggerFactory.getLogger(PostSearchService::class.java)!!

    @CircuitBreaker(name = "elasticsearch", fallbackMethod = "searchFallback")
    fun search(keyword: String?, techStack: String?, status: StudyPostStatus?, maxMembers: Int, pageable: Pageable): Page<PostSearchResult> {
        return postSearchPort.search(keyword, techStack, status, maxMembers, pageable)
    }

    @CircuitBreaker(name = "elasticsearch", fallbackMethod = "indexFallback")
    fun index(post: StudyPost) {
        postSearchPort.index(post)
    }

    @CircuitBreaker(name = "elasticsearch", fallbackMethod = "indexAllFallback")
    fun indexAll(posts: List<StudyPost>) {
        postSearchPort.indexAll(posts)
    }

    @CircuitBreaker(name = "elasticsearch", fallbackMethod = "deleteFallback")
    fun delete(id: String) {
        postSearchPort.delete(UUID.fromString(id))
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    private fun searchFallback(keyword: String?, techStack: String?, status: StudyPostStatus?, maxMembers: Int, pageable: Pageable, e: Exception): Page<PostSearchResult> {
        log.warn("Elasticsearch 장애로 검색 불가. keyword={}", keyword, e)
        return Page.empty(pageable)
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    private fun indexFallback(post: StudyPost, e: Exception) {
        log.warn("Elasticsearch 장애로 인덱싱 불가. postId={}", post.id, e)
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    private fun indexAllFallback(posts: List<StudyPost>, e: Exception) {
        log.warn("Elasticsearch 장애로 전체 인덱싱 불가. size={}", posts.size, e)
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    private fun deleteFallback(id: String, e: Exception) {
        log.warn("Elasticsearch 장애로 삭제 불가. id={}", id, e)
    }
}
