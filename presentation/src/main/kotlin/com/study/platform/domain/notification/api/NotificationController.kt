package com.study.platform.domain.notification.api

import com.study.platform.domain.notification.api.doc.NotificationControllerDoc
import com.study.platform.domain.notification.dto.response.NotificationResponse
import com.study.platform.domain.notification.usecase.CountUnreadNotificationsUseCase
import com.study.platform.domain.notification.usecase.FindNotificationsUseCase
import com.study.platform.domain.notification.usecase.MarkAllNotificationsAsReadUseCase
import com.study.platform.domain.notification.usecase.MarkNotificationAsReadUseCase
import com.study.platform.global.response.ApiResponse
import com.study.platform.global.response.SuccessCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val findNotificationsUseCase: FindNotificationsUseCase,
    private val countUnreadNotificationsUseCase: CountUnreadNotificationsUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase,
    private val markAllNotificationsAsReadUseCase: MarkAllNotificationsAsReadUseCase
) : NotificationControllerDoc {

    @GetMapping
    override fun findNotifications(
        @AuthenticationPrincipal userId: UUID,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<NotificationResponse>>> {
        return ApiResponse.success(SuccessCode.NOTIFICATION_LIST, findNotificationsUseCase.execute(userId, pageable))
    }

    @GetMapping("/unread-count")
    override fun countUnread(@AuthenticationPrincipal userId: UUID): ResponseEntity<ApiResponse<Long>> {
        return ApiResponse.success(SuccessCode.NOTIFICATION_UNREAD_COUNT, countUnreadNotificationsUseCase.execute(userId))
    }

    @PatchMapping("/{notificationId}/read")
    override fun markAsRead(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable notificationId: UUID
    ): ResponseEntity<ApiResponse<Void>> {
        markNotificationAsReadUseCase.execute(userId, notificationId)
        return ApiResponse.success(SuccessCode.NOTIFICATION_READ)
    }

    @PatchMapping("/read-all")
    override fun markAllAsRead(@AuthenticationPrincipal userId: UUID): ResponseEntity<ApiResponse<Void>> {
        markAllNotificationsAsReadUseCase.execute(userId)
        return ApiResponse.success(SuccessCode.NOTIFICATION_READ_ALL)
    }
}
