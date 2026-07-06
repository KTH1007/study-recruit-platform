package com.study.platform.support

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer

abstract class AbstractContainerSupport {

    companion object {
        @JvmStatic
        val mysql: MySQLContainer<*> = MySQLContainer<Nothing>("mysql:8.0.36").apply {
            withDatabaseName("platform_test")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        val redis: GenericContainer<*> = GenericContainer<Nothing>("redis:7.4").apply {
            withExposedPorts(6379)
        }

        init {
            mysql.start()
            redis.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun overrideContainerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mysql::getJdbcUrl)
            registry.add("spring.datasource.username", mysql::getUsername)
            registry.add("spring.datasource.password", mysql::getPassword)
            registry.add("spring.data.redis.host", redis::getHost)
            registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
        }
    }
}
