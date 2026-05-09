# Study Recruit Platform

스터디원을 모집하고, 지원, 채팅, 알림까지 하나의 플랫폼에서 처리하는 백엔드 중심 프로젝트입니다.
단순 CRUD를 넘어 동시성 제어, 성능 최적화, 인프라 구성, CI/CD 자동화까지 직접 설계하고 구현했습니다.

---

## 기술 스택

### Backend

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.3, Spring Security, Spring Batch |
| ORM | Spring Data JPA, HikariCP |
| Auth | Kakao OAuth 2.0, JWT |
| Realtime | WebSocket (STOMP / SockJS), SSE |
| Messaging | Apache Kafka (KRaft) |
| Search | Elasticsearch 9.0.2 |
| Cache | Redis 7 |
| Test | JUnit5, MockMvc, Mockito, TestContainers, k6 (부하테스트) |

### Infra & DevOps

| 분류 | 기술 |
|------|------|
| Server | AWS EC2 t3.large |
| Reverse Proxy | Nginx (로드밸런싱, SSL) |
| Container | Docker, Docker Compose |
| CI/CD | GitHub Actions |
| HTTPS | Let's Encrypt (Certbot) |
| Domain | DuckDNS |
| Log | ELK Stack (Logback -> Logstash -> Elasticsearch -> Kibana) |

---

## 시스템 아키텍처

<img width="945" height="531" alt="image" src="https://github.com/user-attachments/assets/637b4536-bde0-4ea9-992c-4526fbcee34f" />


**주요 흐름**

- 클라이언트 -> HTTPS -> Nginx -> App1 / App2 라운드로빈 로드밸런싱
- 카카오 OAuth 2.0 인증 -> JWT 발급
- 댓글/지원 이벤트 -> Kafka 발행 -> 비동기 알림 저장 -> SSE 실시간 전달
- 게시글 단건 조회 -> Redis 캐시 (TTL 10분)
- 게시글 검색 -> Elasticsearch 풀텍스트 검색
- GitHub Actions -> Docker Hub 이미지 푸시 -> EC2 SSH 배포

---

## 주요 기능

### 1. 인증

- Kakao OAuth 2.0 로그인
- JWT Access Token (30분) + Refresh Token (7일, Redis 저장)

### 2. 스터디 모집 게시글

- CRUD (기술스택/모집상태 필터링, 페이징)
- Elasticsearch 풀텍스트 검색 (키워드, 기술스택, 모집상태, 최대인원)
- Spring Batch 만료 게시글 자동 마감 (JpaCursorItemReader 청크 처리)

### 3. 지원 시스템

- 지원/취소/승인/거절
- 비관적 락 (`PESSIMISTIC_WRITE`)으로 동시 지원 레이스컨디션 방지
- `DataIntegrityViolationException` catch -> 중복 지원 409 응답

### 4. 댓글

- 게시글별 댓글 CRUD
- 댓글 작성 시 게시글 작성자에게 알림 전송
- `@nickname` 정규식 파싱 -> 멘션 알림

### 5. 실시간 채팅

- WebSocket + STOMP + Redis Pub/Sub
- 스터디팀 전용 채팅방 (지원 승인 후 자동 생성)

### 6. 실시간 알림

- Kafka 이벤트 발행 -> `@Async` 비동기 처리 -> DB 저장 -> SSE 전송
- 알림 종류: 지원 접수, 지원 승인/거절, 댓글 알림, 멘션, 팀 일정, 마감 D-1 리마인더
- 읽음 처리 / 미읽음 카운트 / 전체 읽음

### 7. 스터디팀 관리

- 지원 승인 시 팀 자동 생성, 리더/멤버 역할 관리
- 팀 일정 CRUD + 일정 등록 시 팀원 전체 알림
- 매일 오전 9시 D-1 리마인더 자동 발송

### 8. 인프라 / CI/CD

- Nginx 로드밸런싱 (App 2대), Let's Encrypt HTTPS, WSS, gzip 압축
- GitHub Actions: PR -> 빌드/테스트, develop 머지 -> 자동 배포

### 9. MDC 기반 요청 추적 로그

- `MdcFilter`: 요청마다 `requestId`, `serverInstance`, `clientIp`, `method`, `uri` 를 MDC에 자동 주입
- `JwtAuthenticationFilter`: JWT 인증 성공 시 `userId` 를 MDC에 추가 주입
- LogstashEncoder 사용 중이므로 MDC 값이 JSON 필드로 자동 직렬화 -> Kibana에서 requestId 기반 필터링 가능
- 기존 비즈니스 코드 수정 없이 모든 로그에 자동 포함

```
INFO  [requestId=abc123] [userId=uuid] [ip=1.2.3.4] [uri=POST /api/applies] 지원 처리 완료
ERROR [requestId=abc123] [userId=uuid] [ip=1.2.3.4] [uri=POST /api/applies] 지원 처리 중 예외 발생
```

### 10. API 멱등성 처리 (중복 요청 방지)

- 클라이언트가 `Idempotency-Key` 헤더를 포함해 요청 시 Redis에 키 저장 (`SET NX`, TTL 24시간)
- 동일 키로 재요청 시 DB 처리 없이 캐시된 응답 즉시 반환
- 네트워크 재시도, 더블 클릭 등으로 인한 중복 지원/결제 방지

```
첫 번째 요청: Idempotency-Key: abc -> 처리 후 응답 Redis 저장
재시도 요청:  Idempotency-Key: abc -> Redis 캐시 응답 반환 (DB 미조회)
```

### 11. Redis 기반 Rate Limiting

- `@RateLimit` 커스텀 어노테이션으로 API별 선택적 적용
- Redis Sorted Set + Lua Script로 Sliding Window 원자적 처리
- 키 구조: `rate:limit:{userId}:{endpoint}` (사용자별 엔드포인트별 독립 제한)
- 제한 초과 시 HTTP 429 Too Many Requests 반환

```java
@RateLimit(limit = 10, windowSeconds = 60)
@PostMapping("/api/applies")
public ResponseEntity<ApplyResponse> apply(...) { ... }
```

---

## 성능 최적화

### 1. HikariCP 커넥션 풀 튜닝

| | Before | After |
|---|---|---|
| 커넥션 풀 | 기본값 10개 | 50개 |
| p(95) 응답시간 | 9.76s | 7.1s (27% 개선) |

기본 커넥션 풀 10개로는 1만 명 동시 요청 시 풀 고갈 -> 대기 누적. 50개로 증가 후 커넥션 대기 감소.

### 2. 비관적 락 (동시 지원 동시성 제어)

| | Before | After |
|---|---|---|
| 20건 동시 지원 성공률 | 5% (레이스컨디션) | 100% (201 or 409) |

`SELECT -> INSERT` 사이 레이스컨디션으로 유니크 제약 위반 시 500 에러 발생.
`PESSIMISTIC_WRITE` 락 + `DataIntegrityViolationException` catch로 해결.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM StudyPost p JOIN FETCH p.author WHERE p.id = :postId")
Optional<StudyPost> findByIdWithAuthorForUpdate(@Param("postId") UUID postId);
```

### 3. 비동기 알림 처리 (@Async)

| | Before | After |
|---|---|---|
| 에러율 | 87% | 2.3% (96% 감소) |
| RPS | 137/s | 940/s (585% 향상) |

댓글 저장 + 알림 DB 저장 + Redis 발행이 동기 처리되어 커넥션 풀 즉시 고갈.
`@Async("notificationExecutor")` 적용으로 알림 처리를 별도 스레드풀로 분리.
`ThreadPoolExecutor.DiscardPolicy`로 풀 포화 시 알림만 조용히 버림 (API 정상 응답 유지).

```yaml
async:
  notification:
    core-pool-size: 5
    max-pool-size: 20
    queue-capacity: 200
```

### 4. Kafka - 알림 유실 방지

| 설정 | 값 | 효과 |
|------|-----|------|
| acks | all | 모든 replica 기록 후 응답, 메시지 유실 방지 |
| retries | 3 | 일시적 브로커 장애 시 자동 재시도 |
| partition | 2 | 병렬 처리로 소비 처리량 향상 |
| commit | manual | 처리 완료 후 오프셋 커밋, 중복 소비 방지 |

기존 Redis Pub/Sub만 사용할 경우 브로커 재시작 시 발행한 알림이 유실됨.
Kafka 도입으로 알림 이벤트를 영속화 - 컨슈머 장애 시 오프셋 기반으로 재처리 가능.

```
Producer (@Async)
  -> Kafka topic: notifications (2 partition)
  -> Consumer (수동 커밋)
  -> DB 저장 + SSE 전송
```

### 5. Elasticsearch - 한국어 게시글 검색

| 설정 | 내용 |
|------|------|
| 분석기 | nori_analyzer (한국어 형태소 분석) |
| 동의어 필터 | 스프링=spring, 자바=java, 리액트=react |
| techStack 필드 | Text (검색) + Keyword (정렬/필터) 멀티 필드 |
| 쿼리 | BoolQuery (must + filter) 복합 조건 |

MySQL LIKE 쿼리로는 "자바"를 검색해도 "Java" 게시글이 검색되지 않고, 형태소 분리 없이 풀스캔 발생.
Elasticsearch nori 분석기 + 동의어 필터로 한국어/영어 혼용 검색을 정확하게 처리.

### 6. Spring Batch - 만료 게시글 일괄 처리

| 설정 | 값 |
|------|-----|
| ItemReader | JpaCursorItemReader (스트리밍, OOM 방지) |
| Chunk size | 1,000 |
| 실행 | 매일 자정 @Scheduled |
| 중복 실행 방지 | runAt JobParameter로 구분 |
| 배치 메타 테이블 | local: always / prod: never |

`@Scheduled`로 단순 벌크 UPDATE 처리 시 수천 건을 한 트랜잭션에서 처리하면 DB 락 경합 발생.
JpaCursorItemReader로 스트리밍 조회 후 Chunk 단위 커밋하여 트랜잭션 범위 최소화.
ES 인덱스도 배치 내 벌크 인덱싱으로 일관성 유지.

### 7. DB 인덱스 최적화

| 테이블 | 인덱스 | 목적 |
|--------|--------|------|
| study_posts | (status, deadline) | 모집 중 게시글 필터링 + 마감임박순 정렬 |
| study_posts | (author_id, created_at) | 내 게시글 조회 |
| applies | (post_id, status, created_at) | 게시글별 지원 목록 |
| notifications | (receiver_id, is_read, created_at) | 알림 목록 + 미읽음 필터 |
| comments | (post_id, created_at) | 댓글 목록 |

p(95): 7.29s -> 6.5s 개선 (데이터 증가 시 효과 극대화 예상)

### 8. gzip 압축

Spring Boot 내장 압축 설정으로 JSON 응답 크기 감소, 네트워크 전송량 절감.

```yaml
server:
  compression:
    enabled: true
    mime-types: application/json
    min-response-size: 1024
```

### 9. Kafka 비동기 ES 동기화

게시글 저장 시 Elasticsearch 동기 인덱싱 -> API 응답 지연 발생.
Kafka 이벤트 발행으로 ES 인덱싱을 비동기 분리하여 게시글 저장 API 응답과 ES 동기화 흐름 분리.

```
게시글 저장 (MySQL) -> Kafka 발행 -> Consumer -> ES 인덱싱
```

### 10. Kafka DLQ (Dead Letter Queue)

Consumer 처리 실패 시 재시도 3회 후 DLQ 토픽으로 격리.
정상 처리 흐름을 막지 않고 실패 메시지를 별도 보관 후 수동 재처리 가능.

| 설정 | 값 |
|------|-----|
| 재시도 횟수 | 3회 (지수 백오프, 1초 시작 x2배) |
| DLQ 토픽 | post-sync.DLT, notification.DLT |
| 처리 방식 | 실패 메시지 격리 후 정상 흐름 유지 |

```
Consumer 실패 -> 지수 백오프 retry 3회 -> DLT topic (post-sync.DLT / notification.DLT)
```

### 11. Circuit Breaker + Graceful Shutdown

- **Circuit Breaker (Resilience4j)**: Elasticsearch / Kafka 외부 서비스 장애 시 빠른 실패 처리, 불필요한 대기 및 스레드 점유 제거 (실패율 50% 초과 시 OPEN, 30초 후 HALF_OPEN)
- **Graceful Shutdown**: 배포 시 처리 중인 요청을 완료한 후 종료 (`server.shutdown=graceful`, `spring.lifecycle.timeout-per-shutdown-phase=30s`)

```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

### 12. Redis 캐싱 (게시글 단건 조회)

| | Before (DB 직접 조회) | After (Redis 캐시) |
|---|---|---|
| 에러율 | 1.07% | 0.52% |
| p(95) | 5.38s | 97.6ms (98% 개선) |
| RPS | 1,825/s | 3,686/s (2배 향상) |
| 평균 응답시간 | - | 24.66ms |

`@Cacheable`로 첫 조회 시 Redis 저장, 이후 DB 미조회. TTL 10분.
`RedisCacheErrorHandler`로 Redis 장애 시 예외 전파 없이 DB 폴백 처리.

### 13. 배포 서버 성능 (AWS EC2)

목록 조회 vs 단건 조회 (Redis 캐시) 비교 - 10,000 VUs

| 지표 | 목록 조회 (MySQL) | 단건 조회 (Redis) |
|------|-----------------|-----------------|
| 에러율 | 71.45% | 23.25% |
| RPS | 408/s | 555/s |
| 성공률 | 28% | 76% |
| 성공 요청 평균 | 2.71s | 912ms |

500 VUs (안정 구간) - 총 50,000건

| 지표 | 결과 |
|------|------|
| 에러율 | 0.01% |
| p(95) | 277ms |
| 성공률 | 99.99% |

EC2 한계: t3.large (2코어) 단일 서버에 7개 서비스 공유. 부하 시 nginx(41%) + app1(40%) + app2(34%) CPU 합계 ~115% -> CPU 병목 확인.
실제 서비스라면 서비스별 인스턴스 분리 (RDS, ElastiCache, MSK, OpenSearch) 필요.

### 14. Rate Limiting (Sliding Window)

| 항목 | 내용 |
|------|------|
| 저장소 | Redis Sorted Set |
| 알고리즘 | Sliding Window |
| 원자성 | Lua Script (ZADD + ZREMRANGEBYSCORE + ZCARD 원자적 실행) |
| 키 구조 | `rate:limit:{userId}:{endpoint}` |
| 적용 방식 | `@RateLimit` 어노테이션 + `HandlerInterceptor` |
| 초과 응답 | HTTP 429 Too Many Requests |

고정 윈도우(Fixed Window)는 윈도우 경계에서 순간 버스트 허용 취약점 존재.
Sliding Window는 현재 시각 기준 정확한 시간 범위를 계산하여 일관된 제한 적용.
Lua Script로 조회-카운트-만료 처리를 원자적으로 실행해 레이스컨디션 방지.

```
요청 -> HandlerInterceptor -> Lua Script 실행 (Redis Sorted Set)
  -> 윈도우 내 요청 수 <= limit: 통과
  -> 윈도우 내 요청 수 > limit: 429 반환
```

---

## 테스트 환경

### TestContainers 기반 Repository 통합 테스트

**기존 문제**

- H2 인메모리 DB 사용 시 MySQL 방언 불일치 (UUID BINARY, 락 문법 등)
- Redis Lua Script, Pub/Sub, TTL 등 고급 기능을 Mock으로 대체 -> 실제 동작 검증 불가
- 테스트 통과 후 배포 환경에서 DB/Redis 관련 버그 발생

**TestContainers를 선택한 이유**

- 실제 MySQL 8.0 / Redis 7 컨테이너를 테스트 시 직접 실행 -> 프로덕션 환경과 완전히 동일
- `@DynamicPropertySource`로 컨테이너 포트를 Spring에 자동 주입 -> 환경 설정 불필요
- GitHub Actions CI 환경에서도 Docker만 있으면 동일하게 동작
- Rate Limiting(Lua Script), 멱등성(SET NX) 등 Redis 고급 기능 실제 검증 가능

**구조**

```java
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    static final MySQLContainer<?> mysql;
    static final GenericContainer<?> redis;

    static {
        mysql = new MySQLContainer<>("mysql:8.0.36");
        redis = new GenericContainer<>("redis:7.4").withExposedPorts(6379);
        mysql.start();
        redis.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }
}
```

- static 블록으로 컨테이너 최초 1회 시작 후 전체 테스트에서 재사용 (테스트 속도 최적화)
- 모든 Repository 통합 테스트는 `AbstractIntegrationTest`를 상속해 동일 환경 보장

---

## 트러블슈팅

### 1. 동시 지원 레이스컨디션

**문제**: 20명이 동시 지원 시 `SELECT -> INSERT` 사이 레이스컨디션 -> DB 유니크 제약 위반 -> 500 에러

**원인**

```
Thread1: existsByPostIdAndApplicantId -> false
Thread2: existsByPostIdAndApplicantId -> false  <- 둘 다 통과
Thread1: INSERT 성공
Thread2: INSERT -> UniqueConstraintViolation -> 500
```

**해결**: 비관적 락 + 예외 처리

- `findByIdWithAuthorForUpdate`: `PESSIMISTIC_WRITE` 락으로 동시 접근 차단
- `DataIntegrityViolationException` catch -> ALREADY_APPLIED 409 응답

**결과**: 20건 동시 지원 성공률 5% -> 100%

---

### 2. 알림 동기 처리 타임아웃

**문제**: 댓글 작성 API 에러율 87%, RPS 137

**원인**: 댓글 저장 + 알림 DB 저장 + Redis 발행이 하나의 트랜잭션에서 동기 처리 -> 커넥션 풀(50개) 즉시 고갈

**해결**

- `@Async("notificationExecutor")`로 알림 처리를 별도 스레드풀로 분리
- `@TransactionalEventListener(phase = AFTER_COMMIT)`으로 트랜잭션 커밋 후 실행 보장
- `DiscardPolicy`로 스레드풀 포화 시 알림만 조용히 버림 (API 정상 응답 유지)

**결과**: 에러율 87% -> 2.3%, RPS 137 -> 940

---

### 3. Nginx 설정 오류 (upstream directive)

**문제**: EC2 배포 시 `nginx: upstream directive is not allowed`

**원인**: `nginx.conf`에 `http {}` 블록 없이 upstream 블록을 최상단에 작성

**해결**

```nginx
events {
    worker_connections 1024;
}
http {
    upstream app {
        server app1:8080;
        server app2:8080;
    }
    ...
}
```

---

### 4. Let's Encrypt 인증서 볼륨 마운트 오류

**문제**: Nginx 컨테이너 시작 시 SSL 인증서 로드 실패

**원인**: `docker-compose.prod.yml`에서 `./certbot/conf` 빈 디렉토리 마운트

**해결**: EC2 호스트의 `/etc/letsencrypt`를 직접 읽기 전용으로 마운트

```yaml
volumes:
  - /etc/letsencrypt:/etc/letsencrypt:ro
```

---

### 5. EC2 CPU 병목 분석

**문제**: JVM 힙 256MB -> 2GB 증가 후에도 성능 개선 없음

**원인 분석** (`top` 명령어)

```
nginx : 41.5% CPU
app1  : 40.2% CPU
app2  : 33.9% CPU
합계  : ~115% (2코어 = 200% 기준)
st    : 5.4%  <- t3 버스터블 CPU 크레딧 소진으로 스로틀링
```

메모리 여유(262MB free) -> 메모리가 아닌 CPU 2코어가 실제 병목.

**결론**: t3.large 버스터블 인스턴스 특성상 지속 부하 시 CPU 크레딧 소진 -> 스로틀링 발생.
실제 서비스라면 서비스별 인스턴스 분리 + c5 계열(비버스터블) 사용 필요.

---

### 6. SSE 멀티 인스턴스 알림 유실

**문제**: App1에서 발행한 알림이 App2에 연결된 클라이언트에게 전달되지 않음

**원인**: SSE 연결은 각 인스턴스 메모리에 독립 저장 -> Nginx 라운드로빈으로 알림 발행 인스턴스와 SSE 연결 인스턴스가 다를 수 있음

```
클라이언트 -> App1 (SSE 연결)
알림 이벤트 -> App2에서 처리 -> App2 메모리에만 발행 -> App1 클라이언트 수신 불가
```

**해결**: RedisNotificationSubscriber 활성화

모든 인스턴스가 Redis 채널을 구독 -> 어느 인스턴스에서 발행해도 전체 인스턴스로 전파 -> 각 인스턴스가 자신에 연결된 SSE 클라이언트에게 전송

```
알림 이벤트 -> Redis Pub/Sub 발행 -> App1, App2 모두 수신 -> 각자 SSE 전송
```

**결과**: 멀티 인스턴스 환경에서 SSE 알림 유실 0

---

### 7. 데드락 및 타임아웃 처리

**문제**: 비관적 락(`PESSIMISTIC_WRITE`) 사용 시 두 트랜잭션이 서로의 락을 기다리는 데드락 발생 가능

**원인**

```
Transaction A: 게시글 락 획득 -> 지원 행 락 대기
Transaction B: 지원 행 락 획득 -> 게시글 락 대기
-> 서로 대기 -> 데드락
```

**해결**

- `@QueryHints`로 락 획득 타임아웃 3초 설정 -> 타임아웃 초과 시 `LockTimeoutException` 발생 후 503 응답
- 락 획득 순서를 항상 `게시글 -> 지원` 단방향으로 고정해 순환 대기 조건 제거

```java
@QueryHints(@QueryHint(
    name = "jakarta.persistence.lock.timeout",
    value = "3000"
))
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<StudyPost> findByIdWithAuthorForUpdate(@Param("postId") UUID postId);
```

**결과**: 데드락 발생 시 무한 대기 없이 3초 내 실패 응답, 락 순서 고정으로 데드락 발생 자체 제거

---

## TDD / DDD 적용

### 도메인 주도 설계 (DDD)

- 비즈니스 로직을 서비스가 아닌 **도메인 객체**에 위임
- `Apply.approve()`, `Apply.reject()`, `StudyPost.markFull()` 등 상태 전이를 엔티티 메서드로 캡슐화
- 서비스 간 직접 의존 금지 -> **Spring 이벤트**(`ApplicationEventPublisher`)로 결합 제거
- 알림 타입별 **전략 패턴**: `NotificationHandler` 인터페이스 구현체를 타입별로 등록, 새 알림 타입 추가 시 Handler 클래스만 추가

```java
// 도메인 로직을 엔티티에 위임
apply.approve();  // 상태 검증 + 전이를 Apply 내부에서 처리

// 서비스 간 결합 제거 - 이벤트로 분리
eventPublisher.publishEvent(new ApplyApprovedEvent(...));
// ApplyService는 알림 로직을 모름, NotificationService가 @EventListener로 수신
```

### 테스트 주도 개발 (TDD)

- **Repository 통합 테스트**: TestContainers(MySQL 8.0 + Redis 7)로 실제 환경과 동일한 조건에서 검증
- **Service 단위 테스트**: Mockito + BDDMockito로 의존성 격리, 비즈니스 로직 집중 검증
- **Controller 단위 테스트**: `@WebMvcTest` + MockMvc로 HTTP 레이어 독립 검증
- given-when-then 패턴 + 한글 메서드명으로 테스트 의도 명확화

```
Service 단위 테스트  -> Mockito Mock, 비즈니스 규칙 검증
Repository 통합 테스트 -> TestContainers, 실제 쿼리/락/Redis 동작 검증
Controller 단위 테스트 -> MockMvc, HTTP 상태코드/응답 구조 검증
```
