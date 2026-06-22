package com.study.platform.support.fake

import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.post.model.StudyPostStatus
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.UUID

class FakeStudyPostRepository : StudyPostRepository {

    private val store: MutableMap<UUID, StudyPost> = HashMap()

    override fun save(post: StudyPost): StudyPost {
        if (post.id == null) {
            ReflectionTestUtils.setField(post, "id", UUID.randomUUID())
        }
        if (post.createdAt == null) {
            ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.now())
        }
        ReflectionTestUtils.setField(post, "updatedAt", LocalDateTime.now())
        store[post.id!!] = post
        return post
    }

    override fun saveAll(posts: List<StudyPost>): List<StudyPost> {
        posts.forEach { save(it) }
        return posts
    }

    override fun delete(post: StudyPost) {
        store.remove(post.id)
    }

    override fun findById(id: UUID): StudyPost? = store[id]

    override fun findByIdWithAuthor(postId: UUID): StudyPost? = store[postId]

    override fun findByIdWithAuthorForUpdate(postId: UUID): StudyPost? = store[postId]

    override fun findExpiredPosts(now: LocalDateTime, status: StudyPostStatus): List<StudyPost> {
        throw UnsupportedOperationException("필요 시 구현")
    }

    override fun findDeadlineReminderPosts(start: LocalDateTime, end: LocalDateTime, status: StudyPostStatus): List<StudyPost> {
        throw UnsupportedOperationException("필요 시 구현")
    }
}
