FROM mcr.microsoft.com/openjdk/jdk:25-ubuntu AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY common common
COPY domain domain
COPY application application
COPY infrastructure infrastructure
COPY presentation presentation
COPY bootstrap bootstrap
RUN chmod +x gradlew && ./gradlew :bootstrap:bootJar -x test -x generateJooq && rm -f bootstrap/build/libs/*-plain.jar

FROM mcr.microsoft.com/openjdk/jdk:25-ubuntu
WORKDIR /app
COPY --from=builder /app/bootstrap/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-Xms512m", "-Xmx2g", "-jar", "app.jar"]
