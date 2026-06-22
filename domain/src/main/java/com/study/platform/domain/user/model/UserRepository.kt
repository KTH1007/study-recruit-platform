package com.study.platform.domain.user.model

import java.util.UUID

interface UserRepository {

    fun save(user: User): User
    fun findById(id: UUID): User?
    fun findByKakaoId(kakaoId: String): User?
    fun findByNickname(nickname: String): User?
    fun findAllByNicknameIn(nicknames: Collection<String>): List<User>
}
