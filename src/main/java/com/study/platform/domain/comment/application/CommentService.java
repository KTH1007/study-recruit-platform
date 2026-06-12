package com.study.platform.domain.comment.application;

import com.study.platform.domain.comment.dto.request.CommentCreateRequest;
import com.study.platform.domain.comment.dto.request.CommentUpdateRequest;
import com.study.platform.domain.comment.dto.response.CommentResponse;
import com.study.platform.domain.comment.event.CommentCreatedEvent;
import com.study.platform.domain.comment.event.MentionEvent;
import com.study.platform.domain.comment.model.Comment;
import com.study.platform.domain.comment.model.CommentRepository;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.event.DomainEventPublisher;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\S+)");

    private final CommentRepository commentRepository;
    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    public Page<CommentResponse> findComments(UUID postId, Pageable pageable) {
        return commentRepository.findAllByPostIdWithAuthor(postId, pageable)
                .map(CommentResponse::from);
    }

    @Transactional
    public CommentResponse createComment(UUID userId, UUID postId, CommentCreateRequest request) {
        StudyPost post = getPostWithAuthor(postId);
        User commenter = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Comment comment = Comment.create(post, commenter, request.content());
        commentRepository.save(comment);

        publishCommentCreatedEvent(post, commenter);
        publishMentionEvents(request.content(), commenter, post);

        return CommentResponse.from(comment);
    }

    @Transactional
    public CommentResponse updateComment(UUID userId, UUID commentId, CommentUpdateRequest request) {
        Comment comment = getCommentWithAuthor(commentId);
        comment.validateAuthor(userId);
        comment.update(request.content());
        return CommentResponse.from(comment);
    }

    @Transactional
    public void deleteComment(UUID userId, UUID commentId) {
        Comment comment = getCommentWithAuthor(commentId);
        comment.validateAuthor(userId);
        commentRepository.delete(comment);
    }

    // 방장이 아닌 경우에만 방장에게 알림 발행
    private void publishCommentCreatedEvent(StudyPost post, User commenter) {
        if (post.isAuthor(commenter.getId())) {
            return;
        }
        eventPublisher.publish(new CommentCreatedEvent(
                post.getId(),
                post.getAuthor().getId(),
                commenter.getId(),
                post.getTitle()
        ));
    }

    // 댓글 내용에서 @닉네임 파싱 후 해당 유저에게 멘션 알림 발행
    private void publishMentionEvents(String content, User commenter, StudyPost post) {
        Set<String> mentionedNicknames = parseMentions(content);
        userRepository.findAllByNicknameIn(mentionedNicknames).forEach(mentionedUser -> {
            if (mentionedUser.getId().equals(commenter.getId())) {
                return;
            }
            eventPublisher.publish(new MentionEvent(
                    post.getId(),
                    mentionedUser.getId(),
                    commenter.getNickname(),
                    post.getTitle()
            ));
        });
    }

    private Set<String> parseMentions(String content) {
        Matcher matcher = MENTION_PATTERN.matcher(content);
        Set<String> nicknames = new HashSet<>();
        while (matcher.find()) {
            nicknames.add(matcher.group(1));
        }
        return nicknames;
    }

    private StudyPost getPostWithAuthor(UUID postId) {
        return studyPostRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private Comment getCommentWithAuthor(UUID commentId) {
        return commentRepository.findByIdWithAuthor(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
    }
}
