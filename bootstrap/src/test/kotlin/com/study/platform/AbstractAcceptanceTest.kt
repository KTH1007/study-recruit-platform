package com.study.platform

import com.study.platform.domain.post.document.PostSearchRepository
import com.study.platform.domain.user.model.User
import com.study.platform.domain.user.model.UserRepository
import com.study.platform.global.jwt.JwtProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.ResponseErrorHandler
import java.net.URI
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.client.RestTemplate
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class AbstractAcceptanceTest {

    @LocalServerPort
    private var port: Int = 0

    protected val restTemplate = RestTemplate(HttpComponentsClientHttpRequestFactory()).apply {
        errorHandler = object : ResponseErrorHandler {
            override fun hasError(response: ClientHttpResponse) = false
            override fun handleError(url: URI, method: HttpMethod, response: ClientHttpResponse) {}
        }
    }

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jwtProvider: JwtProvider

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
        val kafka: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))

        init {
            mysql.start()
            redis.start()
            kafka.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun overrideProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mysql::getJdbcUrl)
            registry.add("spring.datasource.username", mysql::getUsername)
            registry.add("spring.datasource.password", mysql::getPassword)
            registry.add("spring.data.redis.host", redis::getHost)
            registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
            registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers)
        }
    }

    fun url(path: String) = "http://localhost:$port$path"

    fun createUser(): User {
        val unique = UUID.randomUUID().toString().take(8)
        return userRepository.save(User.create("kakao-$unique", "유저_$unique", "$unique@test.com"))
    }

    fun authHeaders(user: User): HttpHeaders = HttpHeaders().apply {
        set(HttpHeaders.AUTHORIZATION, "Bearer ${jwtProvider.generateAccessToken(user.id!!)}")
        contentType = MediaType.APPLICATION_JSON
    }

    fun createPost(user: User, title: String = "스터디 모집"): Any? {
        val request = com.study.platform.domain.post.dto.request.StudyPostCreateRequest(
            title = title,
            description = "스터디 설명",
            techStack = null,
            maxMembers = 5,
            deadline = LocalDateTime.now().plusDays(7)
        )
        return (restTemplate.exchange(
            url("/api/posts"), HttpMethod.POST,
            HttpEntity(request, authHeaders(user)),
            Map::class.java
        ).body!!["data"] as Map<*, *>)["id"]
    }

}