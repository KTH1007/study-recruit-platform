package com.study.platform.domain.notification.infrastructure

import com.study.platform.domain.notification.model.FailedNotification
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FailedNotificationJpaRepository : JpaRepository<FailedNotification, UUID>
