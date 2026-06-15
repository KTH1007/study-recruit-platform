package com.study.platform.domain.comment.application;

import com.study.platform.domain.comment.dto.request.CommentCreateRequest;
import com.study.platform.domain.comment.dto.response.CommentResponse;
import com.study.platform.domain.comment.event.MentionEvent;
import com.study.platform.domain.comment.model.Comment;
import com.study.platform.domain.comment.model.CommentRepository;
import com.study.platform.domain.comment.usecase.CreateCommentUseCase;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.event.DomainEventPublisher;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CreateCommentService implements CreateCommentUseCase {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\S+)");

    private final CommentRepository commentRepository;
    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public CommentResponse execute(UUID userId, UUID postId, CommentCreateRequest request) {
        StudyPost post = studyPostRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        User commenter = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Comment comment = Comment.create(post, commenter, request.content(), eventPublisher);
        commentRepository.save(comment);

        publishMentionEvents(request.content(), commenter, post);

        return CommentResponse.from(comment);
    }

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
}
