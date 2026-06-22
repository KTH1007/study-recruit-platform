package com.study.platform.domain.notification.infrastructure

import com.study.platform.domain.notification.dto.response.NotificationResponse
import com.study.platform.domain.notification.model.NotificationType
import com.study.platform.domain.notification.port.NotificationQueryPort
import com.study.platform.global.jooq.JooqUtils
import org.jooq.DSLContext
import org.jooq.SortField
import org.jooq.impl.DSL
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
        val orderFields = buildOrderFields(pageable)

        val content = dsl
            .select(
                DSL.field("BIN_TO_UUID(id)").`as`("id"),
                DSL.field("BIN_TO_UUID(receiver_id)").`as`("receiverId"),
                DSL.field("type").`as`("type"),
                DSL.field("message").`as`("message"),
                DSL.field("BIN_TO_UUID(target_id)").`as`("targetId"),
                DSL.field("is_read").`as`("isRead"),
                DSL.field("created_at").`as`("createdAt")
            )
            .from(DSL.table("notifications"))
            .where(JooqUtils.uuidEq("receiver_id", receiverId))
            .orderBy(orderFields)
            .limit(pageable.pageSize)
            .offset(pageable.offset)
            .fetch { r ->
                NotificationResponse(
                    UUID.fromString(r.get("id", String::class.java)),
                    UUID.fromString(r.get("receiverId", String::class.java)),
                    NotificationType.valueOf(r.get("type", String::class.java)),
                    r.get("message", String::class.java),
                    toUuidOrNull(r.get("targetId", String::class.java)),
                    r.get("isRead", Boolean::class.java),
                    r.get("createdAt", LocalDateTime::class.java)
                )
            }

        val total = dsl.fetchCount(
            dsl.selectOne()
                .from(DSL.table("notifications"))
                .where(JooqUtils.uuidEq("receiver_id", receiverId))
        )

        return PageImpl(content, pageable, total.toLong())
    }

    override fun countByReceiverIdAndIsReadFalse(receiverId: UUID): Long =
        dsl.fetchCount(
            dsl.selectOne()
                .from(DSL.table("notifications"))
                .where(JooqUtils.uuidEq("receiver_id", receiverId))
                .and(DSL.field("is_read").eq(false))
        ).toLong()

    private fun buildOrderFields(pageable: Pageable): List<SortField<*>> {
        val orderFields = mutableListOf<SortField<*>>()
        pageable.sort.forEach { order ->
            val column = toColumnName(order.property)
            orderFields.add(if (order.isAscending) DSL.field(column).asc() else DSL.field(column).desc())
        }
        if (orderFields.isEmpty()) {
            orderFields.add(DSL.field("created_at").desc())
        }
        return orderFields
    }

    private fun toColumnName(property: String): String = when (property) {
        "createdAt" -> "created_at"
        "isRead" -> "is_read"
        else -> "created_at"
    }

    private fun toUuidOrNull(value: String?): UUID? =
        value?.let { UUID.fromString(it) }
}
