package com.study.platform.domain.post.infrastructure

import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.post.model.StudyPostStatus
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class StudyPostRepositoryAdapter(
    private val studyPostJpaRepository: StudyPostJpaRepository
) : StudyPostRepository {

    override fun save(post: StudyPost): StudyPost =
        studyPostJpaRepository.saveAndFlush(post)

    override fun saveAll(posts: List<StudyPost>): List<StudyPost> =
        studyPostJpaRepository.saveAll(posts)

    override fun delete(post: StudyPost) {
        studyPostJpaRepository.delete(post)
    }

    override fun findById(id: UUID): StudyPost? =
        studyPostJpaRepository.findById(id).orElse(null)

    override fun findByIdWithAuthor(postId: UUID): StudyPost? =
        studyPostJpaRepository.findByIdWithAuthor(postId)

    override fun findExpiredPosts(now: LocalDateTime, status: StudyPostStatus): List<StudyPost> =
        studyPostJpaRepository.findExpiredPosts(now, status)

    override fun findDeadlineReminderPosts(start: LocalDateTime, end: LocalDateTime, status: StudyPostStatus): List<StudyPost> =
        studyPostJpaRepository.findDeadlineReminderPosts(start, end, status)

    override fun findByIdWithAuthorForUpdate(postId: UUID): StudyPost? =
        studyPostJpaRepository.findByIdWithAuthorForUpdate(postId)
}
