package com.study.platform.domain.notification.api;

import com.study.platform.domain.notification.api.doc.NotificationControllerDoc;
import com.study.platform.domain.notification.application.NotificationService;
import com.study.platform.domain.notification.dto.response.NotificationResponse;
import com.study.platform.global.response.ApiResponse;
import com.study.platform.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationControllerDoc {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> findNotifications(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(SuccessCode.NOTIFICATION_LIST, notificationService.findNotifications(userId, pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> countUnread(@AuthenticationPrincipal UUID userId) {
        return ApiResponse.success(SuccessCode.NOTIFICATION_UNREAD_COUNT, notificationService.countUnread(userId));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal UUID userId, @PathVariable UUID notificationId) {
        notificationService.markAsRead(userId, notificationId);
        return ApiResponse.success(SuccessCode.NOTIFICATION_READ);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal UUID userId) {
        notificationService.markAllAsRead(userId);
        return ApiResponse.success(SuccessCode.NOTIFICATION_READ_ALL);
    }
}
