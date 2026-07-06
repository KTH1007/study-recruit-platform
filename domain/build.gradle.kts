plugins {
    `java-test-fixtures`
    id("org.jetbrains.kotlin.plugin.jpa") version "2.3.0"
}

dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter")
    compileOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")

    testFixturesImplementation(project(":common"))
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-redis")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-kafka")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
    testFixturesImplementation("org.testcontainers:testcontainers:1.21.4")
    testFixturesImplementation("org.testcontainers:mysql:1.21.4")
}
