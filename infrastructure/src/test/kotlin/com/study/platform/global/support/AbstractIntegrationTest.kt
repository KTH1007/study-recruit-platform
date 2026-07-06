package com.study.platform.global.support

import com.study.platform.domain.post.document.PostSearchRepository
import com.study.platform.support.AbstractContainerSupport
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
@ActiveProfiles("test")
abstract class AbstractIntegrationTest : AbstractContainerSupport() {

    @Autowired
    protected lateinit var em: EntityManager

    @MockitoBean
    protected lateinit var postSearchRepository: PostSearchRepository
}
