package com.study.platform.domain.apply.infrastructure;

import com.study.platform.domain.apply.dto.response.ApplyResponse;
import com.study.platform.domain.apply.model.ApplyStatus;
import com.study.platform.domain.apply.port.ApplyQueryPort;
import com.study.platform.global.jooq.JooqUtils;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ApplyQueryRepositoryAdapter implements ApplyQueryPort {

    private final DSLContext dsl;

    @Override
    public List<ApplyResponse> findAllByPostId(UUID postId) {
        return dsl
                .select(
                        DSL.field("BIN_TO_UUID(a.id)").as("id"),
                        DSL.field("u.nickname").as("applicantNickname"),
                        DSL.field("u.tech_stack").as("applicantTechStack"),
                        DSL.field("a.message").as("message"),
                        DSL.field("a.status").as("status"),
                        DSL.field("a.created_at").as("createdAt")
                )
                .from(DSL.table("applies").as("a"))
                .join(DSL.table("users").as("u"))
                .on(DSL.field("a.applicant_id").eq(DSL.field("u.id")))
                .where(JooqUtils.uuidEq("a.post_id", postId))
                .fetch(r -> new ApplyResponse(
                        UUID.fromString(r.get("id", String.class)),
                        r.get("applicantNickname", String.class),
                        r.get("applicantTechStack", String.class),
                        r.get("message", String.class),
                        ApplyStatus.valueOf(r.get("status", String.class)),
                        r.get("createdAt", LocalDateTime.class)
                ));
    }
}
