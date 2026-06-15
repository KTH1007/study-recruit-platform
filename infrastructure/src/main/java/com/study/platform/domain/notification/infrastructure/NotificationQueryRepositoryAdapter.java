package com.study.platform.domain.notification.infrastructure;

import com.study.platform.domain.notification.dto.response.NotificationResponse;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.domain.notification.port.NotificationQueryPort;
import com.study.platform.global.jooq.JooqUtils;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepositoryAdapter implements NotificationQueryPort {

    private final DSLContext dsl;

    @Override
    public Page<NotificationResponse> findAllByReceiverId(UUID receiverId, Pageable pageable) {
        List<SortField<?>> orderFields = buildOrderFields(pageable);

        List<NotificationResponse> content = dsl
                .select(
                        DSL.field("BIN_TO_UUID(id)").as("id"),
                        DSL.field("BIN_TO_UUID(receiver_id)").as("receiverId"),
                        DSL.field("type").as("type"),
                        DSL.field("message").as("message"),
                        DSL.field("BIN_TO_UUID(target_id)").as("targetId"),
                        DSL.field("is_read").as("isRead"),
                        DSL.field("created_at").as("createdAt")
                )
                .from(DSL.table("notifications"))
                .where(JooqUtils.uuidEq("receiver_id", receiverId))
                .orderBy(orderFields)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch(r -> new NotificationResponse(
                        UUID.fromString(r.get("id", String.class)),
                        UUID.fromString(r.get("receiverId", String.class)),
                        NotificationType.valueOf(r.get("type", String.class)),
                        r.get("message", String.class),
                        toUuidOrNull(r.get("targetId", String.class)),
                        r.get("isRead", Boolean.class),
                        r.get("createdAt", LocalDateTime.class)
                ));

        int total = dsl.fetchCount(
                dsl.selectOne()
                        .from(DSL.table("notifications"))
                        .where(JooqUtils.uuidEq("receiver_id", receiverId))
        );

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public long countByReceiverIdAndIsReadFalse(UUID receiverId) {
        return dsl.fetchCount(
                dsl.selectOne()
                        .from(DSL.table("notifications"))
                        .where(JooqUtils.uuidEq("receiver_id", receiverId))
                        .and(DSL.field("is_read").eq(false))
        );
    }

    private List<SortField<?>> buildOrderFields(Pageable pageable) {
        List<SortField<?>> orderFields = new ArrayList<>();
        pageable.getSort().forEach(order -> {
            String column = toColumnName(order.getProperty());
            orderFields.add(order.isAscending()
                    ? DSL.field(column).asc()
                    : DSL.field(column).desc());
        });
        if (orderFields.isEmpty()) {
            orderFields.add(DSL.field("created_at").desc());
        }
        return orderFields;
    }

    private String toColumnName(String property) {
        return switch (property) {
            case "createdAt" -> "created_at";
            case "isRead" -> "is_read";
            default -> "created_at";
        };
    }

    private UUID toUuidOrNull(String value) {
        return value != null ? UUID.fromString(value) : null;
    }
}
