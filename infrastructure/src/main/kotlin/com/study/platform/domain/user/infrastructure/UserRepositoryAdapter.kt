package com.study.platform.domain.user.infrastructure

import com.study.platform.domain.user.model.User
import com.study.platform.domain.user.model.UserRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepositoryAdapter(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {

    override fun save(user: User): User =
        userJpaRepository.save(user)

    override fun findById(id: UUID): User? =
        userJpaRepository.findById(id).orElse(null)

    override fun findByKakaoId(kakaoId: String): User? =
        userJpaRepository.findByKakaoId(kakaoId)

    override fun findByNickname(nickname: String): User? =
        userJpaRepository.findByNickname(nickname)

    override fun findAllByNicknameIn(nicknames: Collection<String>): List<User> =
        userJpaRepository.findAllByNicknameIn(nicknames)
}
