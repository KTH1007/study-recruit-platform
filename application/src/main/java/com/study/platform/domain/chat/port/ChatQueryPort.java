package com.study.platform.domain.chat.port;

import com.study.platform.domain.chat.dto.response.ChatMessageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface ChatQueryPort {

    Slice<ChatMessageResponse> findMessagesByTeamId(UUID teamId, Pageable pageable);
}
