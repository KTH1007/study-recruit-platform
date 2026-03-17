package com.study.platform.domain.user.api;

import com.study.platform.domain.user.api.doc.AuthControllerDoc;
import com.study.platform.domain.user.application.AuthService;
import com.study.platform.domain.user.dto.request.LoginRequest;
import com.study.platform.domain.user.dto.request.TokenReissueRequest;
import com.study.platform.domain.user.dto.response.LoginResponse;
import com.study.platform.global.response.ApiResponse;
import com.study.platform.global.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDoc {

    private final AuthService authService;

    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoLogin(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.kakaoLogin(request.code());
        return ApiResponse.success(SuccessCode.USER_LOGIN, response);
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<LoginResponse>> reissueToken(@Valid @RequestBody TokenReissueRequest request) {
        LoginResponse response = authService.reissueToken(request);
        return ApiResponse.success(SuccessCode.TOKEN_REISSUED, response);
    }
}
