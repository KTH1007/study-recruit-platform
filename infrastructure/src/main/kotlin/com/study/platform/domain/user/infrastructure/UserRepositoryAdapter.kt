package com.study.platform.domain.user.infrastructure

import com.study.platform.domain.user.model.User
import com.study.platform.domain.user.model.UserRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
class UserRepositoryAdapter(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {

    override fun save(user: User): User =
        userJpaRepository.save(user)

    // 신규 유저 생성은 별도 트랜잭션으로 분리해, unique 제약 위반 시 flush 실패가
    // 호출자의 트랜잭션(예: AuthService.kakaoLogin)의 영속성 컨텍스트를 오염시키지 않도록 한다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun saveNew(user: User): User =
        userJpaRepository.saveAndFlush(user)

    override fun findById(id: UUID): User? =
        userJpaRepository.findById(id).orElse(null)

    override fun findByKakaoId(kakaoId: String): User? =
        userJpaRepository.findByKakaoId(kakaoId)

    override fun findByNickname(nickname: String): User? =
        userJpaRepository.findByNickname(nickname)

    override fun findAllByNicknameIn(nicknames: Collection<String>): List<User> =
        userJpaRepository.findAllByNicknameIn(nicknames)
}
