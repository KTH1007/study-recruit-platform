package com.study.platform.domain.chat.api;

import com.study.platform.domain.chat.api.doc.ChatHttpControllerDoc;
import com.study.platform.domain.chat.dto.response.ChatMessageResponse;
import com.study.platform.domain.chat.usecase.FindChatMessagesUseCase;
import com.study.platform.global.response.ApiResponse;
import com.study.platform.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class ChatHttpController implements ChatHttpControllerDoc {

    private final FindChatMessagesUseCase findChatMessagesUseCase;

    @GetMapping("/{teamId}/chat")
    public ResponseEntity<ApiResponse<Slice<ChatMessageResponse>>> findMessages(
            @PathVariable UUID teamId,
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 30) Pageable pageable) {
        return ApiResponse.success(SuccessCode.CHAT_MESSAGE_LIST,
                findChatMessagesUseCase.execute(userId, teamId, pageable));
    }
}
