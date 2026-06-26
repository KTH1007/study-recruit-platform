# Study Recruit Platform

스터디원 모집부터 지원, 실시간 채팅, 알림까지 하나의 플랫폼에서 처리하는 백엔드 프로젝트입니다.

---

## 시스템 아키텍처

!Architecture

---

## 기술 스택

### Backend

| 분류 | 기술 |
|------|------|
| Language | Kotlin |
| Framework | Spring Boot, Spring Security, Spring Batch |
| ORM | Spring Data JPA, jOOQ, HikariCP |
| Auth | Kakao OAuth 2.0, JWT |
| Realtime | WebSocket (STOMP / SockJS), SSE |
| Messaging | Apache Kafka (KRaft), Outbox Pattern |
| Search | Elasticsearch 9.0.2 (nori 분석기) |
| Cache | Redis 7 |
| Test | JUnit5, MockMvc, Mockito, TestContainers, k6 |

### Infra & DevOps

| 분류 | 기술 |
|------|------|
| Server | AWS EC2 t3.large |
| Reverse Proxy | Nginx (로드밸런싱, SSL, gzip) |
| Container | Docker, Docker Compose |
| CI/CD | GitHub Actions |
| HTTPS | Let`s Encrypt (Certbot) |
| Domain | DuckDNS |
| Log | ELK Stack (Logback -> Logstash -> Elasticsearch -> Kibana) |

---

## 핵심 성과

| 문제 | 개선 |
|------|------|
| 댓글 알림 동기 처리로 커넥션 풀 고갈 | 에러율 87% -> 2.3%, RPS 137 -> 940 (585% 향상) |
| 게시글 단건 조회 DB 직접 조회 | p(95) 5.38s -> 97.6ms (98% 개선), RPS 1,825 -> 3,686 |
| 동시 지원 레이스컨디션으로 500 에러 | 비관적 락 적용 후 성공률 5% -> 100% |
| Kafka 발행 실패 시 알림 유실 | Outbox 패턴 + RetryScheduler로 유실 방지 |

---

## 주요 기능

- **인증** — Kakao OAuth 2.0, JWT Access/Refresh Token
- **스터디 모집** — CRUD, Elasticsearch 풀텍스트 검색, Spring Batch 자동 마감
- **지원 시스템** — 비관적 락으로 동시 지원 레이스컨디션 방지
- **실시간 채팅** — WebSocket STOMP + Redis Pub/Sub 멀티 인스턴스 동기화
- **실시간 알림** — Kafka -> Redis Pub/Sub -> SSE, Outbox 패턴으로 유실 방지
- **스터디팀 관리** — 팀 자동 생성, 일정 관리, D-1 리마인더
- **API 보호** — Sliding Window Rate Limiting, Idempotency Key 중복 요청 방지
- **요청 추적** — MDC 기반 requestId/userId 자동 주입 -> Kibana 필터링


---

## Wiki

| 페이지 | 내용 |
|---|---|
| [Architecture](https://github.com/KTH1007/study-recruit-platform/wiki/Architecture) | 시스템 구성, 데이터 흐름 |
| [Features](https://github.com/KTH1007/study-recruit-platform/wiki/Features) | 주요 기능 상세 |
| [Performance](https://github.com/KTH1007/study-recruit-platform/wiki/Performance) | 성능 최적화 |
| [Testing Strategy](https://github.com/KTH1007/study-recruit-platform/wiki/Testing-Strategy) | 테스트 전략 |
| [Tech Decisions](https://github.com/KTH1007/study-recruit-platform/wiki/Tech-Decisions) | 기술 선택 이유와 트레이드오프 |
| [Troubleshooting](https://github.com/KTH1007/study-recruit-platform/wiki/Troubleshooting) | 문제 해결 사례 |
| [Deployment](https://github.com/KTH1007/study-recruit-platform/wiki/Deployment) | 배포 프로세스 |
| [장애 시나리오별 대응](https://github.com/KTH1007/study-recruit-platform/wiki/장애-시나리오별-대응) | Kafka, Redis, DB 장애 시 현재 동작과 한계 |
