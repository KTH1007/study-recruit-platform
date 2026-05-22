package com.study.platform.domain.notification.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FailedNotificationRepository extends JpaRepository<FailedNotification, UUID> {
}