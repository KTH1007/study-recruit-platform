package com.study.platform.global.response

import org.springframework.http.HttpStatus

enum class SuccessCode(val httpStatus: HttpStatus, val message: String) {

    // Common
    OK(HttpStatus.OK, "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED, "리소스가 성공적으로 생성되었습니다."),
    NO_CONTENT(HttpStatus.NO_CONTENT, "성공적으로 삭제되었습니다."),

    // User
    USER_REGISTERED(HttpStatus.CREATED, "회원가입이 완료되었습니다."),
    USER_LOGIN(HttpStatus.OK, "로그인이 완료되었습니다."),
    USER_LOGOUT(HttpStatus.OK, "로그아웃이 완료되었습니다."),
    USER_INFO(HttpStatus.OK, "회원 정보 조회가 완료되었습니다."),
    TOKEN_REISSUED(HttpStatus.OK, "토큰이 재발급되었습니다."),

    // Post
    POST_CREATED(HttpStatus.CREATED, "모집글이 등록되었습니다."),
    POST_UPDATED(HttpStatus.OK, "모집글이 수정되었습니다."),
    POST_DELETED(HttpStatus.OK, "모집글이 삭제되었습니다."),
    POST_LIST(HttpStatus.OK, "모집글 목록 조회가 완료되었습니다."),
    POST_DETAIL(HttpStatus.OK, "모집글 상세 조회가 완료되었습니다."),

    // Apply
    APPLY_CREATED(HttpStatus.CREATED, "지원이 완료되었습니다."),
    APPLY_CANCELED(HttpStatus.OK, "지원이 취소되었습니다."),
    APPLY_APPROVED(HttpStatus.OK, "지원이 승인되었습니다."),
    APPLY_REJECTED(HttpStatus.OK, "지원이 거절되었습니다."),
    APPLY_LIST(HttpStatus.OK, "지원 목록 조회가 완료되었습니다."),

    // Comment
    COMMENT_CREATED(HttpStatus.CREATED, "댓글이 등록되었습니다."),
    COMMENT_UPDATED(HttpStatus.OK, "댓글이 수정되었습니다."),
    COMMENT_DELETED(HttpStatus.OK, "댓글이 삭제되었습니다."),
    COMMENT_LIST(HttpStatus.OK, "댓글 목록 조회가 완료되었습니다."),

    // Notification
    NOTIFICATION_LIST(HttpStatus.OK, "알림 목록 조회가 완료되었습니다."),
    NOTIFICATION_READ(HttpStatus.OK, "알림이 읽음 처리되었습니다."),
    NOTIFICATION_READ_ALL(HttpStatus.OK, "모든 알림이 읽음 처리되었습니다."),
    NOTIFICATION_UNREAD_COUNT(HttpStatus.OK, "미읽음 알림 수 조회가 완료되었습니다."),

    // Team
    TEAM_FOUND(HttpStatus.OK, "팀 정보 조회가 완료되었습니다."),
    TEAM_MEMBER_LIST(HttpStatus.OK, "팀원 목록 조회가 완료되었습니다."),
    LEADER_DELEGATED(HttpStatus.OK, "리더 위임이 완료되었습니다."),
    TEAM_MEMBER_REMOVED(HttpStatus.OK, "팀원이 추방되었습니다."),
    TEAM_LEFT(HttpStatus.OK, "팀에서 탈퇴하였습니다."),
    TEAM_SCHEDULE_CREATED(HttpStatus.CREATED, "팀 일정이 등록되었습니다."),
    TEAM_SCHEDULE_UPDATED(HttpStatus.OK, "팀 일정이 수정되었습니다."),
    TEAM_SCHEDULE_DELETED(HttpStatus.OK, "팀 일정이 삭제되었습니다."),
    TEAM_SCHEDULE_LIST(HttpStatus.OK, "팀 일정 목록 조회가 완료되었습니다."),

    // Chat
    CHAT_MESSAGE_LIST(HttpStatus.OK, "채팅 내역 조회가 완료되었습니다.")
}
