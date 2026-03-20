package com.study.platform.domain.user.model;

import com.study.platform.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String kakaoId;

    @Column(length = 50, nullable = false, unique = true)
    private String nickname;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 255)
    private String techStack;

    @Builder
    private User(String kakaoId, String nickname, String email, String techStack) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.email = email;
        this.techStack = techStack;
    }

    public static User create(String kakaoId, String nickname, String email) {
        return User.builder()
                .kakaoId(kakaoId)
                .nickname(nickname)
                .email(email)
                .build();
    }
}
