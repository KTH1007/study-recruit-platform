package com.study.platform.domain.notification.infrastructure

import com.study.platform.domain.notification.dto.response.NotificationResponse
import com.study.platform.domain.notification.model.NotificationType
import com.study.platform.domain.notification.port.NotificationQueryPort
import com.study.platform.global.jooq.JooqUtils.binToUuid
import com.study.platform.global.jooq.JooqUtils.uuidEq
import com.study.platform.jooq.tables.references.NOTIFICATIONS
import org.jooq.DSLContext
import org.jooq.SortField
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class NotificationQueryRepositoryAdapter(
    private val dsl: DSLContext
) : NotificationQueryPort {

    override fun findAllByReceiverId(receiverId: UUID, pageable: Pageable): Page<NotificationResponse> {
        val n = NOTIFICATIONS.`as`("n")
        val orderFields = buildOrderFields(n, pageable)

        val content = dsl.select(
            binToUuid(n.ID).`as`("id"),
            binToUuid(n.RECEIVER_ID).`as`("receiverId"),
            n.TYPE.`as`("type"),
            n.MESSAGE.`as`("message"),
            binToUuid(n.TARGET_ID).`as`("targetId"),
            n.IS_READ.`as`("isRead"),
            n.CREATED_AT.`as`("createdAt")
        )
            .from(n)
            .where(uuidEq(n.RECEIVER_ID, receiverId))
            .orderBy(orderFields)
            .limit(pageable.pageSize)
            .offset(pageable.offset)
            .fetch { r ->
                NotificationResponse(
                    UUID.fromString(r.get("id", String::class.java)),
                    UUID.fromString(r.get("receiverId", String::class.java)),
                    NotificationType.valueOf(r.get("type", String::class.java)),
                    r.get("message", String::class.java),
                    r.get("targetId", String::class.java)?.let { UUID.fromString(it) },
                    r.get("isRead", Boolean::class.java),
                    r.get("createdAt", LocalDateTime::class.java)
                )
            }

        val total = dsl.fetchCount(
            dsl.selectOne().from(n).where(uuidEq(n.RECEIVER_ID, receiverId))
        )

        return PageImpl(content, pageable, total.toLong())
    }

    override fun countByReceiverIdAndIsReadFalse(receiverId: UUID): Long {
        val n = NOTIFICATIONS.`as`("n")
        return dsl.fetchCount(
            dsl.selectOne()
                .from(n)
                .where(uuidEq(n.RECEIVER_ID, receiverId))
                .and(n.IS_READ.eq(false))
        ).toLong()
    }

    private fun buildOrderFields(n: com.study.platform.jooq.tables.Notifications, pageable: Pageable): List<SortField<*>> {
        val orderFields = pageable.sort.map { order ->
            when (order.property) {
                "isRead" -> if (order.isAscending) n.IS_READ.asc() else n.IS_READ.desc()
                else -> if (order.isAscending) n.CREATED_AT.asc() else n.CREATED_AT.desc()
            }
        }.toMutableList<SortField<*>>()
        if (orderFields.isEmpty()) orderFields.add(n.CREATED_AT.desc())
        return orderFields
    }
}
