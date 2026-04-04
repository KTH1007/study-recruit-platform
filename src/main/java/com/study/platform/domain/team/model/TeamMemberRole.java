package com.study.platform.domain.team.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TeamMemberRole {

    LEADER("팀장"),
    MEMBER("팀원");

    private final String description;
}
