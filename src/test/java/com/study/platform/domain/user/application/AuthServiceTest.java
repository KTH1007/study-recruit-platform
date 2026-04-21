package com.study.platform.domain.user.application;

import com.study.platform.domain.user.dto.request.TokenReissueRequest;
import com.study.platform.domain.user.dto.response.LoginResponse;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.global.jwt.JwtProvider;
import com.study.platform.global.oauth.kakao.KakaoOAuthClient;
import com.study.platform.global.oauth.kakao.dto.KakaoIdTokenPayload;
import com.study.platform.global.oauth.kakao.dto.KakaoTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtProvider jwtProvider;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private KakaoOAuthClient kakaoOAuthClient;

    @InjectMocks
    private AuthService authService;

    private UUID userId;
    private User user;
    private KakaoTokenResponse kakaoTokenResponse;
    private KakaoIdTokenPayload kakaoIdTokenPayload;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.create("kakao-123", "테스트유저", "test@test.com");
        ReflectionTestUtils.setField(user, "id", userId);

        kakaoTokenResponse = new KakaoTokenResponse("access", "bearer", "refresh", 3600, "id-token");
        kakaoIdTokenPayload = new KakaoIdTokenPayload("kakao-123", "test@test.com", "테스트유저");
    }

    @Test
    void kakaoLogin_신규유저_회원가입후로그인() {
        // given
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(kakaoOAuthClient.getToken("auth-code")).willReturn(kakaoTokenResponse);
        given(kakaoOAuthClient.parseIdToken("id-token")).willReturn(kakaoIdTokenPayload);
        given(userRepository.findByKakaoId("kakao-123")).willReturn(Optional.empty());
        given(userRepository.findByNickname("테스트유저")).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(user);
        given(jwtProvider.generateAccessToken(userId)).willReturn("access-token");
        given(jwtProvider.generateRefreshToken(userId)).willReturn("refresh-token");

        // when
        LoginResponse response = authService.kakaoLogin("auth-code");

        // then
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        then(userRepository).should().save(any(User.class));
    }

    @Test
    void kakaoLogin_기존유저_로그인() {
        // given
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(kakaoOAuthClient.getToken("auth-code")).willReturn(kakaoTokenResponse);
        given(kakaoOAuthClient.parseIdToken("id-token")).willReturn(kakaoIdTokenPayload);
        given(userRepository.findByKakaoId("kakao-123")).willReturn(Optional.of(user));
        given(jwtProvider.generateAccessToken(userId)).willReturn("access-token");
        given(jwtProvider.generateRefreshToken(userId)).willReturn("refresh-token");

        // when
        LoginResponse response = authService.kakaoLogin("auth-code");

        // then
        assertThat(response.accessToken()).isEqualTo("access-token");
        then(userRepository).should().findByKakaoId("kakao-123");
        then(userRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void kakaoLogin_닉네임중복_suffix추가후저장() {
        // given
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        User duplicateNicknameUser = User.create("kakao-456", "테스트유저", "other@test.com");
        given(kakaoOAuthClient.getToken("auth-code")).willReturn(kakaoTokenResponse);
        given(kakaoOAuthClient.parseIdToken("id-token")).willReturn(kakaoIdTokenPayload);
        given(userRepository.findByKakaoId("kakao-123")).willReturn(Optional.empty());
        given(userRepository.findByNickname("테스트유저")).willReturn(Optional.of(duplicateNicknameUser));
        given(userRepository.findByNickname("테스트유저_1")).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(user);
        given(jwtProvider.generateAccessToken(userId)).willReturn("access-token");
        given(jwtProvider.generateRefreshToken(userId)).willReturn("refresh-token");

        // when
        LoginResponse response = authService.kakaoLogin("auth-code");

        // then
        assertThat(response).isNotNull();
        then(userRepository).should().findByNickname("테스트유저_1");
    }

    @Test
    void reissueToken_성공() {
        // given
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        String refreshToken = "valid-refresh-token";
        given(jwtProvider.getUserIdFromToken(refreshToken)).willReturn(userId);
        given(valueOps.get("refreshToken:" + userId)).willReturn(refreshToken);
        given(jwtProvider.generateAccessToken(userId)).willReturn("new-access-token");
        given(jwtProvider.generateRefreshToken(userId)).willReturn("new-refresh-token");

        // when
        LoginResponse response = authService.reissueToken(new TokenReissueRequest(refreshToken));

        // then
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void reissueToken_저장된토큰불일치_예외발생() {
        // given
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        String refreshToken = "valid-refresh-token";
        given(jwtProvider.getUserIdFromToken(refreshToken)).willReturn(userId);
        given(valueOps.get("refreshToken:" + userId)).willReturn("other-token");

        // when & then
        assertThatThrownBy(() -> authService.reissueToken(new TokenReissueRequest(refreshToken)))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_TOKEN.getMessage());
    }

    @Test
    void logout_성공() {
        // when
        authService.logout(userId);

        // then
        then(redisTemplate).should().delete("refreshToken:" + userId);
    }
}
