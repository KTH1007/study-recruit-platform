package com.study.platform.domain.user.api.doc

import com.study.platform.domain.user.dto.request.LoginRequest
import com.study.platform.domain.user.dto.request.TokenReissueRequest
import com.study.platform.domain.user.dto.response.LoginResponse
import com.study.platform.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

@Tag(name = "Auth", description = "인증 API")
interface AuthControllerDoc {

    @Operation(summary = "카카오 로그인", description = "카카오 인가 코드로 로그인합니다. 신규 유저면 자동 회원가입됩니다.")
    fun kakaoLogin(@RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>>

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰으로 액세스 토큰을 재발급합니다.")
    fun reissueToken(@RequestBody request: TokenReissueRequest): ResponseEntity<ApiResponse<LoginResponse>>

    @Operation(summary = "로그아웃", description = "Refresh Token을 삭제합니다.")
    fun logout(@Parameter(hidden = true) userId: UUID): ResponseEntity<ApiResponse<Void>>
}
