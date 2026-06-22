package com.study.platform.domain.notification.api.doc

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID

@Tag(name = "SSE", description = "실시간 알림 구독 API")
interface SseControllerDoc {

    @Operation(summary = "SSE 구독", description = "실시간 알림을 수신하기 위한 SSE 연결을 맺습니다. 연결 유지 시간은 30분입니다.")
    fun subscribe(userId: UUID): SseEmitter
}
