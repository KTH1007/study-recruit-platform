package com.study.platform.domain.notification.api.doc

import com.study.platform.domain.notification.dto.response.NotificationResponse
import com.study.platform.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import java.util.UUID

@Tag(name = "Notification", description = "알림 API")
interface NotificationControllerDoc {

    @Operation(summary = "알림 목록 조회", description = "내 알림 목록을 페이징으로 조회합니다.")
    @Parameters(
        Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
        Parameter(name = "size", description = "페이지 크기", example = "20"),
        Parameter(name = "sort", description = "정렬 기준 (createdAt,desc)", example = "createdAt,desc")
    )
    fun findNotifications(userId: UUID, @Parameter(hidden = true) pageable: Pageable): ResponseEntity<ApiResponse<Page<NotificationResponse>>>

    @Operation(summary = "미읽음 알림 수 조회", description = "읽지 않은 알림 개수를 조회합니다.")
    fun countUnread(userId: UUID): ResponseEntity<ApiResponse<Long>>

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    fun markAsRead(userId: UUID, notificationId: UUID): ResponseEntity<ApiResponse<Void>>

    @Operation(summary = "전체 알림 읽음 처리", description = "모든 알림을 읽음 처리합니다.")
    fun markAllAsRead(userId: UUID): ResponseEntity<ApiResponse<Void>>
}
