package com.study.platform.domain.chat.api

import com.study.platform.domain.chat.api.doc.ChatHttpControllerDoc
import com.study.platform.domain.chat.dto.response.ChatMessageResponse
import com.study.platform.domain.chat.usecase.FindChatMessagesUseCase
import com.study.platform.global.response.ApiResponse
import com.study.platform.global.response.SuccessCode
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/teams")
class ChatHttpController(
    private val findChatMessagesUseCase: FindChatMessagesUseCase
) : ChatHttpControllerDoc {

    @GetMapping("/{teamId}/chat")
    override fun findMessages(
        @PathVariable teamId: UUID,
        @AuthenticationPrincipal userId: UUID,
        @PageableDefault(size = 30) pageable: Pageable
    ): ResponseEntity<ApiResponse<Slice<ChatMessageResponse>>> {
        return ApiResponse.success(SuccessCode.CHAT_MESSAGE_LIST,
            findChatMessagesUseCase.execute(userId, teamId, pageable))
    }
}
