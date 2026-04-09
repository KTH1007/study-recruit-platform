package com.study.platform.domain.chat.api.doc;

import com.study.platform.domain.chat.dto.response.ChatMessageResponse;
import com.study.platform.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Tag(name = "Chat", description = "채팅 API")
public interface ChatHttpControllerDoc {

    @Operation(summary = "채팅 내역 조회", description = "팀방의 채팅 내역을 최신순으로 조회합니다.")
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "페이지 크기", example = "30")
    })
    ResponseEntity<ApiResponse<Slice<ChatMessageResponse>>> findMessages(
            @Parameter(description = "팀 ID") @PathVariable UUID teamId,
            @Parameter(hidden = true) @AuthenticationPrincipal UUID userId,
            @Parameter(hidden = true) Pageable pageable
    );
}