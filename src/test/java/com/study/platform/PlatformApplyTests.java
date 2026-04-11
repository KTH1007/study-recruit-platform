package com.study.platform;

import com.study.platform.domain.post.application.PostSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchAutoConfiguration," +
                "org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchRepositoriesAutoConfiguration"
})
class PlatformApplicationTests {

    @MockitoBean
    PostSearchService postSearchService;

    @Test
    void contextLoads() {
    }
}