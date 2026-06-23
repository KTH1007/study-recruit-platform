package com.study.platform.domain.user.api

import com.study.platform.domain.user.api.doc.AuthControllerDoc
import com.study.platform.domain.user.application.AuthService
import com.study.platform.domain.user.dto.request.LoginRequest
import com.study.platform.domain.user.dto.request.TokenReissueRequest
import com.study.platform.domain.user.dto.response.LoginResponse
import com.study.platform.global.response.ApiResponse
import com.study.platform.global.response.SuccessCode
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) : AuthControllerDoc {

    @PostMapping("/kakao")
    override fun kakaoLogin(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val response = authService.kakaoLogin(request.code)
        return ApiResponse.success(SuccessCode.USER_LOGIN, response)
    }

    @PostMapping("/reissue")
    override fun reissueToken(@Valid @RequestBody request: TokenReissueRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val response = authService.reissueToken(request)
        return ApiResponse.success(SuccessCode.TOKEN_REISSUED, response)
    }

    @DeleteMapping("/logout")
    override fun logout(@AuthenticationPrincipal userId: UUID): ResponseEntity<ApiResponse<Void>> {
        authService.logout(userId)
        return ApiResponse.success(SuccessCode.USER_LOGOUT)
    }
}
