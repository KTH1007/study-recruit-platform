package com.study.platform.domain.notification.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.platform.domain.notification.dto.response.NotificationResponse;
import com.study.platform.domain.notification.model.Notification;
import com.study.platform.domain.notification.model.NotificationRepository;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final String NOTIFICATION_CHANNEL = "notification";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(UUID receiverId, NotificationType type, String message, UUID postId) {
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Notification notification = Notification.create(receiver, type, message, postId);
        notificationRepository.save(notification);
        publishToRedis(NotificationResponse.from(notification));
    }

    public Page<NotificationResponse> findNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findAllByReceiverId(userId, pageable)
                .map(NotificationResponse::from);
    }

    public long countUnread(UUID userId) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
        validateReceiver(notification, userId);
        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId);
    }

    private void publishToRedis(NotificationResponse response) {
        try {
            String payload = objectMapper.writeValueAsString(response);
            redisTemplate.convertAndSend(NOTIFICATION_CHANNEL, payload);
        } catch (JsonProcessingException e) {
            log.warn("알림 Redis 발행 실패 : {}", e.getMessage());
        }
    }

    private void validateReceiver(Notification notification, UUID userId) {
        if (!notification.getReceiver().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

}
