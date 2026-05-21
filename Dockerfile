FROM mcr.microsoft.com/openjdk/jdk:25-ubuntu AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x gradlew && ./gradlew bootJar -x test && rm -f build/libs/*-plain.jar

FROM mcr.microsoft.com/openjdk/jdk:25-ubuntu
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-Xms512m", "-Xmx2g", "-jar", "app.jar"]