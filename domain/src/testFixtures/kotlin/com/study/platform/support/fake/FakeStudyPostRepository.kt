package com.study.platform.support.fake

import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.post.model.StudyPostStatus
import java.time.LocalDateTime
import java.util.UUID

class FakeStudyPostRepository : AbstractFakeUuidRepository<StudyPost>(), StudyPostRepository {

    override fun idOf(entity: StudyPost): UUID? = entity.id
    override fun hasTimestamps() = true

    override fun save(post: StudyPost): StudyPost = saveEntity(post)

    override fun saveAll(posts: List<StudyPost>): List<StudyPost> {
        posts.forEach { save(it) }
        return posts
    }

    override fun delete(post: StudyPost) = deleteEntity(post)

    override fun findByIdWithAuthor(postId: UUID): StudyPost? = store[postId]

    override fun findByIdWithAuthorForUpdate(postId: UUID): StudyPost? = store[postId]

    override fun findDeadlineReminderPosts(start: LocalDateTime, end: LocalDateTime, status: StudyPostStatus): List<StudyPost> =
        store.values.filter { p ->
            p.status == status && p.deadline?.let { d -> !d.isBefore(start) && !d.isAfter(end) } == true
        }
}
