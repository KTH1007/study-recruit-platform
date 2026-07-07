package com.study.platform.support.fake

import com.study.platform.domain.user.model.User
import com.study.platform.domain.user.model.UserRepository
import java.util.UUID

class FakeUserRepository : AbstractFakeUuidRepository<User>(), UserRepository {

    override fun idOf(entity: User): UUID? = entity.id

    override fun save(user: User): User = saveEntity(user)

    override fun saveNew(user: User): User = saveEntity(user)

    override fun findByKakaoId(kakaoId: String): User? =
        store.values.firstOrNull { u -> u.kakaoId == kakaoId }

    override fun findByNickname(nickname: String): User? =
        store.values.firstOrNull { u -> u.nickname == nickname }

    override fun findAllByNicknameIn(nicknames: Collection<String>): List<User> =
        store.values.filter { u -> nicknames.contains(u.nickname) }
}
