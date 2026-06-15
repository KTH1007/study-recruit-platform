package com.study.platform.domain.team.infrastructure;

import com.study.platform.domain.team.dto.response.TeamScheduleResponse;
import com.study.platform.domain.team.port.TeamScheduleQueryPort;
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
public class TeamScheduleQueryRepositoryAdapter implements TeamScheduleQueryPort {

    private final DSLContext dsl;

    @Override
    public List<TeamScheduleResponse> findAllByTeamId(UUID teamId) {
        return dsl
                .select(
                        DSL.field("BIN_TO_UUID(s.id)").as("id"),
                        DSL.field("BIN_TO_UUID(s.team_id)").as("teamId"),
                        DSL.field("s.title").as("title"),
                        DSL.field("s.description").as("description"),
                        DSL.field("s.scheduled_at").as("scheduledAt"),
                        DSL.field("s.created_at").as("createdAt")
                )
                .from(DSL.table("team_schedules").as("s"))
                .where(JooqUtils.uuidEq("s.team_id", teamId))
                .orderBy(DSL.field("s.scheduled_at").asc())
                .fetch(r -> new TeamScheduleResponse(
                        UUID.fromString(r.get("id", String.class)),
                        UUID.fromString(r.get("teamId", String.class)),
                        r.get("title", String.class),
                        r.get("description", String.class),
                        r.get("scheduledAt", LocalDateTime.class),
                        r.get("createdAt", LocalDateTime.class)
                ));
    }
}
