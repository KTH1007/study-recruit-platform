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
| Realtime | WebSocket (STOMP), SSE |
| Messaging | Apache Kafka (KRaft) |
| Search | Elasticsearch 9.0.2 |
| Cache | Redis 7 |
| Test | k6 (부하테스트) |

### Infra & DevOps

| 분류 | 기술 |
|------|------|
| Server | AWS EC2 t3.large |
| Reverse Proxy | Nginx (로드밸런싱, SSL) |
| Container | Docker, Docker Compose |
| CI/CD | GitHub Actions |
| HTTPS | Let's Encrypt (Certbot) |
| Domain | DuckDNS |

---

## 시스템 아키텍처


**주요 흐름**

- 클라이언트 → HTTPS → Nginx → App1 / App2 라운드로빈 로드밸런싱
- 카카오 OAuth 2.0 인증 → JWT 발급
- 댓글/지원 이벤트 → Kafka 발행 → 비동기 알림 저장 → SSE 실시간 전달
- 게시글 단건 조회 → Redis 캐시 (TTL 10분)
- 게시글 검색 → Elasticsearch 풀텍스트 검색
- GitHub Actions → Docker Hub 이미지 푸시 → EC2 SSH 배포

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
- `DataIntegrityViolationException` catch → 중복 지원 409 응답

### 4. 댓글

- 게시글별 댓글 CRUD
- 댓글 작성 시 게시글 작성자에게 알림 전송
- `@nickname` 정규식 파싱 → 멘션 알림

### 5. 실시간 채팅

- WebSocket + STOMP + Redis Pub/Sub
- 스터디팀 전용 채팅방 (지원 승인 후 자동 생성)

### 6. 실시간 알림

- Kafka 이벤트 발행 → `@Async` 비동기 처리 → DB 저장 → SSE 전송
- 알림 종류: 지원 접수, 지원 승인/거절, 댓글 알림, 멘션, 팀 일정, 마감 D-1 리마인더
- 읽음 처리 / 미읽음 카운트 / 전체 읽음

### 7. 스터디팀 관리

- 지원 승인 시 팀 자동 생성, 리더/멤버 역할 관리
- 팀 일정 CRUD + 일정 등록 시 팀원 전체 알림
- 매일 오전 9시 D-1 리마인더 자동 발송

### 8. 인프라 / CI/CD

- Nginx 로드밸런싱 (App 2대), Let's Encrypt HTTPS, WSS
- GitHub Actions: PR → 빌드/테스트, develop 머지 → 자동 배포

---

## 성능 최적화

### 1. HikariCP 커넥션 풀 튜닝

| | Before | After |
|---|---|---|
| 커넥션 풀 | 기본값 10개 | 50개 |
| p(95) 응답시간 | 9.76s | 7.1s (27% 개선) |

기본 커넥션 풀 10개로는 1만 명 동시 요청 시 풀 고갈 → 대기 누적. 50개로 증가 후 커넥션 대기 감소.

### 2. 비관적 락 (동시 지원 동시성 제어)

| | Before | After |
|---|---|---|
| 20건 동시 지원 성공률 | 5% (레이스컨디션) | 100% (201 or 409) |

`SELECT → INSERT` 사이 레이스컨디션으로 유니크 제약 위반 시 500 에러 발생.
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

### 4. DB 인덱스 최적화

| 테이블 | 인덱스 | 목적 |
|--------|--------|------|
| study_posts | (status, deadline) | 모집 중 게시글 필터링 + 마감임박순 정렬 |
| study_posts | (author_id, created_at) | 내 게시글 조회 |
| applies | (post_id, status, created_at) | 게시글별 지원 목록 |
| notifications | (receiver_id, is_read, created_at) | 알림 목록 + 미읽음 필터 |
| comments | (post_id, created_at) | 댓글 목록 |

p(95): 7.29s → 6.5s 개선 (데이터 증가 시 효과 극대화 예상)

### 5. Redis 캐싱 (게시글 단건 조회)

| | Before (DB 직접 조회) | After (Redis 캐시) |
|---|---|---|
| 에러율 | 1.07% | 0.52% |
| p(95) | 5.38s | 97.6ms (98% 개선) |
| RPS | 1,825/s | 3,686/s (2배 향상) |
| 평균 응답시간 | - | 24.66ms |

`@Cacheable`로 첫 조회 시 Redis 저장, 이후 DB 미조회. TTL 10분.
`RedisCacheErrorHandler`로 Redis 장애 시 예외 전파 없이 DB 폴백 처리.

### 6. 배포 서버 성능 (AWS EC2)

목록 조회 vs 단건 조회 (Redis 캐시) 비교 — 10,000 VUs

| 지표 | 목록 조회 (MySQL) | 단건 조회 (Redis) |
|------|-----------------|-----------------|
| 에러율 | 71.45% | 23.25% |
| RPS | 408/s | 555/s |
| 성공률 | 28% | 76% |
| 성공 요청 평균 | 2.71s | 912ms |

500 VUs (안정 구간) — 총 50,000건

| 지표 | 결과 |
|------|------|
| 에러율 | 0.01% |
| p(95) | 277ms |
| 성공률 | 99.99% |

EC2 한계: t3.large (2코어) 단일 서버에 7개 서비스 공유. 부하 시 nginx(41%) + app1(40%) + app2(34%) CPU 합계 ~115% → CPU 병목 확인.
실제 서비스라면 서비스별 인스턴스 분리 (RDS, ElastiCache, MSK, OpenSearch) 필요.

---

## 트러블슈팅

### 1. 동시 지원 레이스컨디션

**문제**: 20명이 동시 지원 시 `SELECT → INSERT` 사이 레이스컨디션 → DB 유니크 제약 위반 → 500 에러

**원인**

```
Thread1: existsByPostIdAndApplicantId → false
Thread2: existsByPostIdAndApplicantId → false  ← 둘 다 통과
Thread1: INSERT 성공
Thread2: INSERT → UniqueConstraintViolation → 500
```

**해결**: 비관적 락 + 예외 처리

- `findByIdWithAuthorForUpdate`: `PESSIMISTIC_WRITE` 락으로 동시 접근 차단
- `DataIntegrityViolationException` catch → ALREADY_APPLIED 409 응답

**결과**: 20건 동시 지원 성공률 5% → 100%

---

### 2. 알림 동기 처리 타임아웃

**문제**: 댓글 작성 API 에러율 87%, RPS 137

**원인**: 댓글 저장 + 알림 DB 저장 + Redis 발행이 하나의 트랜잭션에서 동기 처리 → 커넥션 풀(50개) 즉시 고갈

**해결**

- `@Async("notificationExecutor")`로 알림 처리를 별도 스레드풀로 분리
- `@TransactionalEventListener(phase = AFTER_COMMIT)`으로 트랜잭션 커밋 후 실행 보장
- `DiscardPolicy`로 스레드풀 포화 시 알림만 조용히 버림 (API 정상 응답 유지)

**결과**: 에러율 87% → 2.3%, RPS 137 → 940

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

**문제**: JVM 힙 256MB → 2GB 증가 후에도 성능 개선 없음

**원인 분석** (`top` 명령어)

```
nginx : 41.5% CPU
app1  : 40.2% CPU
app2  : 33.9% CPU
합계  : ~115% (2코어 = 200% 기준)
st    : 5.4%  ← t3 버스터블 CPU 크레딧 소진으로 스로틀링
```

메모리 여유(262MB free) → 메모리가 아닌 CPU 2코어가 실제 병목.

**결론**: t3.large 버스터블 인스턴스 특성상 지속 부하 시 CPU 크레딧 소진 → 스로틀링 발생.
실제 서비스라면 서비스별 인스턴스 분리 + c5 계열(비버스터블) 사용 필요.
