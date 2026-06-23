package com.study.platform.domain.apply.model

import com.study.platform.domain.apply.event.ApplyApprovedEvent
import com.study.platform.domain.apply.event.ApplyReceivedEvent
import com.study.platform.domain.apply.event.ApplyRejectedEvent
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.user.model.User
import com.study.platform.global.event.DomainEventPublisher
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import java.time.LocalDateTime
import java.util.UUID

class Apply private constructor(
    val id: UUID,
    val post: StudyPost,
    val applicant: User,
    val message: String,
    var status: ApplyStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime? = null
){
    fun approve(publisher: DomainEventPublisher) {
        validatePending()
        status = ApplyStatus.APPROVED
        publisher.publish(ApplyApprovedEvent(post.id!!, applicant.id!!, post.title))
    }

    fun reject(publisher: DomainEventPublisher) {
        validatePending()
        status = ApplyStatus.REJECTED
        publisher.publish(ApplyRejectedEvent(post.id!!, applicant.id!!, post.title))
    }

    private fun validatePending() {
        if (status != ApplyStatus.PENDING) {
            throw CustomException(ErrorCode.APPLICATION_ALREADY_PROCESSED)
        }
    }

    companion object {
        fun create(post: StudyPost, applicant: User, message: String, publisher: DomainEventPublisher): Apply {
            val apply = Apply(
                id = UUID.randomUUID(),
                post = post,
                applicant = applicant,
                message = message,
                status = ApplyStatus.PENDING,
                createdAt = LocalDateTime.now()
            )
            publisher.publish(ApplyReceivedEvent(post.id!!, post.author!!.id!!, post.title))
            return apply
        }

        fun reconstitute(
            id: UUID, post: StudyPost, applicant: User,
            message: String, status: ApplyStatus,
            createdAt: LocalDateTime, updatedAt: LocalDateTime?
        ): Apply = Apply(id, post, applicant, message, status, createdAt, updatedAt)
    }
}
