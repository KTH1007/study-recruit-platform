package com.study.platform.domain.comment.application

import com.study.platform.domain.comment.dto.request.CommentCreateRequest
import com.study.platform.domain.comment.dto.response.CommentResponse
import com.study.platform.domain.comment.event.MentionEvent
import com.study.platform.domain.comment.model.Comment
import com.study.platform.domain.comment.model.CommentRepository
import com.study.platform.domain.comment.usecase.CreateCommentUseCase
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.user.model.User
import com.study.platform.domain.user.model.UserRepository
import com.study.platform.global.event.DomainEventPublisher
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import java.util.regex.Pattern

@Service
class CreateCommentService(
    private val commentRepository: CommentRepository,
    private val studyPostRepository: StudyPostRepository,
    private val userRepository: UserRepository,
    private val eventPublisher: DomainEventPublisher
) : CreateCommentUseCase {

    @Transactional
    override fun execute(userId: UUID, postId: UUID, request: CommentCreateRequest): CommentResponse {
        val post = studyPostRepository.findById(postId)
            ?: throw CustomException(ErrorCode.POST_NOT_FOUND)
        val commenter = userRepository.findById(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val comment = Comment.create(post, commenter, request.content, eventPublisher)
        commentRepository.save(comment)

        publishMentionEvents(request.content, commenter, post.id!!, post.title)

        return CommentResponse.from(comment)
    }

    private fun publishMentionEvents(content: String, commenter: User, postId: UUID, postTitle: String) {
        val mentionedNicknames = parseMentions(content)
        userRepository.findAllByNicknameIn(mentionedNicknames).forEach { mentionedUser ->
            if (mentionedUser.id == commenter.id) return@forEach
            eventPublisher.publish(MentionEvent(
                postId,
                mentionedUser.id!!,
                commenter.nickname,
                postTitle
            ))
        }
    }

    private fun parseMentions(content: String): Set<String> {
        val matcher = MENTION_PATTERN.matcher(content)
        val nicknames = mutableSetOf<String>()
        while (matcher.find()) {
            nicknames.add(matcher.group(1))
        }
        return nicknames
    }

    companion object {
        private val MENTION_PATTERN: Pattern = Pattern.compile("@(\\S+)")
    }
}
