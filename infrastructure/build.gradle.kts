plugins {
    id("org.jetbrains.kotlin.plugin.jpa") version "2.3.0"
    id("nu.studer.jooq") version "9.0"
}

sourceSets {
    main {
        kotlin.srcDir("src/main/generated")
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    runtimeOnly("com.mysql:mysql-connector-j")
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
    jooqGenerator("com.mysql:mysql-connector-j")

    testImplementation(testFixtures(project(":domain")))
    testImplementation("org.testcontainers:testcontainers:1.21.4")
    testImplementation("org.testcontainers:mysql:1.21.4")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
}

jooq {
    version = "3.19.30"
    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc = org.jooq.meta.jaxb.Jdbc().apply {
                    driver = "com.mysql.cj.jdbc.Driver"
                    url = "jdbc:mysql://localhost:3307/study_platform?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul"
                    user = "root"
                    password = "root"
                }
                generator = org.jooq.meta.jaxb.Generator().apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database = org.jooq.meta.jaxb.Database().apply {
                        name = "org.jooq.meta.mysql.MySQLDatabase"
                        inputSchema = "study_platform"
                        isOutputSchemaToDefault = true
                    }
                    target = org.jooq.meta.jaxb.Target().apply {
                        packageName = "com.study.platform.jooq"
                        directory = "src/main/generated"
                    }
                }
            }
        }
    }
}
