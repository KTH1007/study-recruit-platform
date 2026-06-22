package com.study.platform.support.fake

import com.study.platform.domain.user.model.User
import com.study.platform.domain.user.model.UserRepository
import org.springframework.test.util.ReflectionTestUtils
import java.util.UUID

class FakeUserRepository : UserRepository {

    private val store: MutableMap<UUID, User> = HashMap()

    override fun save(user: User): User {
        if (user.id == null) {
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID())
        }
        store[user.id!!] = user
        return user
    }

    override fun findById(id: UUID): User? = store[id]

    override fun findByKakaoId(kakaoId: String): User? =
        store.values.firstOrNull { u -> u.kakaoId == kakaoId }

    override fun findByNickname(nickname: String): User? =
        store.values.firstOrNull { u -> u.nickname == nickname }

    override fun findAllByNicknameIn(nicknames: Collection<String>): List<User> =
        store.values.filter { u -> nicknames.contains(u.nickname) }
}
