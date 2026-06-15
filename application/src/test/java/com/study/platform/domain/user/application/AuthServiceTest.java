package com.study.platform.domain.user.application;

import com.study.platform.domain.user.dto.request.TokenReissueRequest;
import com.study.platform.domain.user.dto.response.LoginResponse;
import com.study.platform.domain.user.model.User;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.support.fake.FakeOAuthClient;
import com.study.platform.support.fake.FakeTokenManager;
import com.study.platform.support.fake.FakeTokenRepository;
import com.study.platform.support.fake.FakeUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

    private FakeUserRepository userRepository;
    private FakeTokenManager tokenManager;
    private FakeTokenRepository tokenRepository;
    private FakeOAuthClient oAuthClient;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        tokenManager = new FakeTokenManager();
        tokenRepository = new FakeTokenRepository();
        oAuthClient = new FakeOAuthClient();
        authService = new AuthService(userRepository, tokenManager, tokenRepository, oAuthClient);
    }

    @Test
    void kakaoLogin_신규유저_회원가입후로그인() {
        // given
        oAuthClient.register("auth-code", "kakao-123", "테스트유저", "test@test.com");

        // when
        LoginResponse response = authService.kakaoLogin("auth-code");

        // then
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(userRepository.findByKakaoId("kakao-123")).isPresent();
    }

    @Test
    void kakaoLogin_기존유저_로그인() {
        // given
        oAuthClient.register("auth-code", "kakao-123", "테스트유저", "test@test.com");
        userRepository.save(User.create("kakao-123", "테스트유저", "test@test.com"));

        // when
        LoginResponse response = authService.kakaoLogin("auth-code");

        // then
        assertThat(response.accessToken()).isNotBlank();
        // 기존 유저 재사용 - 동일 kakaoId로 신규 저장되지 않음
        assertThat(userRepository.findAllByNicknameIn(java.util.List.of("테스트유저"))).hasSize(1);
    }

    @Test
    void kakaoLogin_닉네임중복_suffix추가후저장() {
        // given
        oAuthClient.register("auth-code", "kakao-123", "테스트유저", "test@test.com");
        userRepository.save(User.create("kakao-456", "테스트유저", "other@test.com"));

        // when
        authService.kakaoLogin("auth-code");

        // then
        assertThat(userRepository.findByNickname("테스트유저_1")).isPresent();
    }

    @Test
    void reissueToken_성공() {
        // given
        oAuthClient.register("auth-code", "kakao-123", "테스트유저", "test@test.com");
        LoginResponse firstLogin = authService.kakaoLogin("auth-code");
        String refreshToken = firstLogin.refreshToken();

        // when
        LoginResponse response = authService.reissueToken(new TokenReissueRequest(refreshToken));

        // then
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
    }

    @Test
    void reissueToken_저장된토큰불일치_예외발생() {
        // given
        UUID userId = UUID.randomUUID();
        String refreshToken = tokenManager.generateRefreshToken(userId);
        tokenRepository.save("refreshToken:" + userId, "other-token", Duration.ofDays(7));

        // when & then
        assertThatThrownBy(() -> authService.reissueToken(new TokenReissueRequest(refreshToken)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);
    }

    @Test
    void reissueToken_유효하지않은토큰_예외발생() {
        // given
        String invalidToken = "invalid-token";

        // when & then
        assertThatThrownBy(() -> authService.reissueToken(new TokenReissueRequest(invalidToken)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);
    }

    @Test
    void logout_성공() {
        // given
        oAuthClient.register("auth-code", "kakao-123", "테스트유저", "test@test.com");
        LoginResponse loginResponse = authService.kakaoLogin("auth-code");
        UUID userId = tokenManager.getUserIdFromToken(loginResponse.refreshToken());

        // when
        authService.logout(userId);

        // then
        assertThat(tokenRepository.find("refreshToken:" + userId)).isEmpty();
    }
}
