package com.study.platform.domain.user.model;

import com.study.platform.global.entity.BaseTimeEntity;
import com.study.platform.global.model.TechStack;
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

    @Getter(AccessLevel.NONE)
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "nickname", length = 50, nullable = false, unique = true))
    private Nickname nickname;

    @Getter(AccessLevel.NONE)
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", length = 100, nullable = false, unique = true))
    private Email email;

    @Getter(AccessLevel.NONE)
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "tech_stack", length = 255))
    private TechStack techStack;

    @Builder
    private User(String kakaoId, Nickname nickname, Email email, TechStack techStack) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.email = email;
        this.techStack = techStack;
    }

    public static User create(String kakaoId, String nickname, String email) {
        return User.builder()
                .kakaoId(kakaoId)
                .nickname(new Nickname(nickname))
                .email(new Email(email))
                .build();
    }

    public String getNickname() {
        return nickname.value();
    }

    public String getEmail() {
        return email.value();
    }

    public String getTechStack() {
        return techStack != null ? techStack.value() : null;
    }
}
