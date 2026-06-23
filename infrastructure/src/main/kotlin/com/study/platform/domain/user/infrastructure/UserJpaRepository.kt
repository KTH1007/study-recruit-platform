package com.study.platform.domain.user.infrastructure

import com.study.platform.domain.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserJpaRepository : JpaRepository<User, UUID> {

    fun findByKakaoId(kakaoId: String): User?

    fun findByNickname(nickname: String): User?

    fun findAllByNicknameIn(nicknames: Collection<String>): List<User>
}
