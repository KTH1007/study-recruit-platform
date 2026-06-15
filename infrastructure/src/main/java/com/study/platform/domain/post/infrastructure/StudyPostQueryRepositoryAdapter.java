package com.study.platform.domain.post.infrastructure;

import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse;
import com.study.platform.domain.post.model.StudyPostStatus;
import com.study.platform.domain.post.port.StudyPostQueryPort;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
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
public class StudyPostQueryRepositoryAdapter implements StudyPostQueryPort {

    private final DSLContext dsl;

    @Override
    public Page<StudyPostSummaryResponse> findAllWithFilter(String techStack, StudyPostStatus status, Pageable pageable) {
        Condition condition = buildCondition(techStack, status);
        List<SortField<?>> orderFields = buildOrderFields(pageable);

        List<StudyPostSummaryResponse> content = dsl
                .select(
                        DSL.field("BIN_TO_UUID(p.id)").as("id"),
                        DSL.field("u.nickname").as("authorNickname"),
                        DSL.field("p.title").as("title"),
                        DSL.field("p.tech_stack").as("techStack"),
                        DSL.field("p.max_members").as("maxMembers"),
                        DSL.field("p.deadline").as("deadline"),
                        DSL.field("p.status").as("status"),
                        DSL.field("p.created_at").as("createdAt")
                )
                .from(DSL.table("study_posts").as("p"))
                .join(DSL.table("users").as("u"))
                .on(DSL.field("p.author_id").eq(DSL.field("u.id")))
                .where(condition)
                .orderBy(orderFields)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch(r -> new StudyPostSummaryResponse(
                        UUID.fromString(r.get("id", String.class)),
                        r.get("authorNickname", String.class),
                        r.get("title", String.class),
                        r.get("techStack", String.class),
                        r.get("maxMembers", Integer.class),
                        r.get("deadline", LocalDateTime.class),
                        StudyPostStatus.valueOf(r.get("status", String.class)),
                        r.get("createdAt", LocalDateTime.class)
                ));

        int total = dsl.fetchCount(
                dsl.selectOne()
                        .from(DSL.table("study_posts").as("p"))
                        .where(condition)
        );

        return new PageImpl<>(content, pageable, total);
    }

    private Condition buildCondition(String techStack, StudyPostStatus status) {
        Condition condition = DSL.trueCondition();
        if (techStack != null && !techStack.isBlank()) {
            condition = condition.and(DSL.field("p.tech_stack").like("%" + techStack + "%"));
        }
        if (status != null) {
            condition = condition.and(DSL.field("p.status").eq(status.name()));
        }
        return condition;
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
            orderFields.add(DSL.field("p.created_at").desc());
        }
        return orderFields;
    }

    private String toColumnName(String property) {
        return switch (property) {
            case "createdAt" -> "p.created_at";
            case "deadline" -> "p.deadline";
            case "status" -> "p.status";
            default -> "p.created_at";
        };
    }
}
