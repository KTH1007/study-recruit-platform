package com.study.platform.global.support

import com.study.platform.domain.post.document.PostSearchRepository
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer

@SpringBootTest
@ActiveProfiles("test")
abstract class AbstractIntegrationTest {

    @Autowired
    protected lateinit var em: EntityManager

    @MockitoBean
    protected lateinit var postSearchRepository: PostSearchRepository

    companion object {
        val mysql: MySQLContainer<*> = MySQLContainer<Nothing>("mysql:8.0.36").apply {
            withDatabaseName("platform_test")
            withUsername("test")
            withPassword("test")
        }
        val redis: GenericContainer<*> = GenericContainer<Nothing>("redis:7.4").apply {
            withExposedPorts(6379)
        }

        init {
            mysql.start()
            redis.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun overrideProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mysql::getJdbcUrl)
            registry.add("spring.datasource.username", mysql::getUsername)
            registry.add("spring.datasource.password", mysql::getPassword)
            registry.add("spring.data.redis.host", redis::getHost)
            registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
        }
    }
}
