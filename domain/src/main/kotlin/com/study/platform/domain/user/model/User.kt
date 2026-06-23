package com.study.platform.domain.user.model

import com.study.platform.global.entity.BaseTimeEntity
import com.study.platform.global.model.TechStack
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "users")
class User : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    var id: UUID? = null

    @Column(nullable = false, unique = true)
    var kakaoId: String = ""

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "nickname", length = 50, nullable = false, unique = true))
    private var nicknameVo: Nickname? = null

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "email", length = 100, nullable = false, unique = true))
    private var emailVo: Email? = null

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "tech_stack", length = 255))
    private var techStackVo: TechStack? = null

    val nickname: String get() = nicknameVo!!.value
    val email: String get() = emailVo!!.value
    val techStack: String? get() = techStackVo?.value

    companion object {
        fun create(kakaoId: String, nickname: String, email: String): User = User().also {
            it.kakaoId = kakaoId
            it.nicknameVo = Nickname(nickname)
            it.emailVo = Email(email)
        }
    }
}
