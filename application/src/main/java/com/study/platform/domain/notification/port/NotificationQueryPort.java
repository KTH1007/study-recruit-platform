package com.study.platform.domain.notification.port;

import com.study.platform.domain.notification.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationQueryPort {

    Page<NotificationResponse> findAllByReceiverId(UUID receiverId, Pageable pageable);

    long countByReceiverIdAndIsReadFalse(UUID receiverId);
}
