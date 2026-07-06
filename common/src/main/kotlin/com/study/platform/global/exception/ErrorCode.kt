package com.study.platform.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val httpStatus: HttpStatus, val message: String) {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),

    // Post
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 모집글입니다."),
    NOT_POST_AUTHOR(HttpStatus.FORBIDDEN, "모집글 작성자만 수행할 수 있습니다."),
    POST_CLOSED(HttpStatus.BAD_REQUEST, "마감된 모집글입니다."),

    // Application
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 지원입니다."),
    ALREADY_APPLIED(HttpStatus.CONFLICT, "이미 지원한 모집글입니다."),
    APPLICATION_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 지원입니다."),
    CANNOT_APPLY_OWN_POST(HttpStatus.BAD_REQUEST, "본인이 작성한 모집글에는 지원할 수 없습니다."),

    // Comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    NOT_COMMENT_AUTHOR(HttpStatus.FORBIDDEN, "댓글 작성자만 수행할 수 있습니다."),

    // Notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다."),

    // Team
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 팀입니다."),
    NOT_TEAM_MEMBER(HttpStatus.FORBIDDEN, "팀원만 접근할 수 있습니다."),
    TEAM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 팀원입니다."),
    LEADER_MUST_DELEGATE(HttpStatus.BAD_REQUEST, "리더 위임 후 탈퇴할 수 있습니다."),
    TEAM_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 일정입니다."),
    CANNOT_REMOVE_SELF(HttpStatus.BAD_REQUEST, "자기 자신은 강퇴할 수 없습니다."),
    CANNOT_DELEGATE_TO_SELF(HttpStatus.BAD_REQUEST, "자기 자신에게는 리더를 위임할 수 없습니다."),

    // Rate Limit
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),

    // Concurrency
    LOCK_CONFLICT(HttpStatus.CONFLICT, "다른 요청이 처리 중입니다. 잠시 후 다시 시도해주세요.")
}
