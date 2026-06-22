package com.study.platform.domain.post.model

import java.time.LocalDateTime
import java.util.UUID

interface StudyPostRepository {

    fun save(post: StudyPost): StudyPost
    fun saveAll(posts: List<StudyPost>): List<StudyPost>
    fun delete(post: StudyPost)
    fun findById(id: UUID): StudyPost?
    fun findByIdWithAuthor(postId: UUID): StudyPost?
    fun findExpiredPosts(now: LocalDateTime, status: StudyPostStatus): List<StudyPost>
    fun findDeadlineReminderPosts(start: LocalDateTime, end: LocalDateTime, status: StudyPostStatus): List<StudyPost>
    fun findByIdWithAuthorForUpdate(postId: UUID): StudyPost?
}
