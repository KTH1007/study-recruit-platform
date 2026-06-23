package com.study.platform.domain.notification.model

enum class NotificationType(val description: String) {
    COMMENT_CREATED("내 게시글에 댓글이 달렸습니다."),
    MENTION("댓글에서 멘션되었습니다."),
    APPLY_RECEIVED("새로운 지원서가 도착했습니다."),
    APPLY_APPROVED("지원서가 승인되었습니다."),
    APPLY_REJECTED("지원서가 거절되었습니다."),
    POST_DEADLINE("게시글 마감이 임박했습니다."),
    SCHEDULE_CREATED("새로운 일정이 등록되었습니다."),
    SCHEDULE_REMINDER("내일 일정이 있습니다.")
}
