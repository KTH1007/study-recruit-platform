plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":infrastructure"))
    implementation(project(":presentation"))
    implementation("org.springframework.boot:spring-boot-starter")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation(project(":common"))
    testImplementation(project(":domain"))
    testImplementation(project(":application"))
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation(testFixtures(project(":domain")))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers:1.21.4")
    testImplementation("org.testcontainers:mysql:1.21.4")
    testImplementation("org.testcontainers:kafka:1.21.4")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
    testImplementation("io.rest-assured:rest-assured:5.5.0")
    testImplementation("org.apache.httpcomponents.client5:httpclient5")
}
