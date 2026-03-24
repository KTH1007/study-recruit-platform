package com.study.platform.domain.notification.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    COMMENT_CREATED("내 게시글에 댓글이 달렸습니다."),
    MENTION("댓글에서 멘션되었습니다."),
    APPLY_RECEIVED("새로운 지원서가 도착했습니다."),
    APPLY_APPROVED("지원서가 승인되었습니다."),
    APPLY_REJECTED("지원서가 거절되었습니다."),
    POST_DEADLINE("게시글 마감이 임박했습니다.");

    private final String description;
}
