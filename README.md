# PaySplit

구독 서비스를 여러 사람이 함께 공유하고 비용을 분담하는 파티 기반 구독 공유 플랫폼입니다.

## 기술 스택

- **Backend**: Java 17, Spring Boot 3.5.9
- **Database**: MySQL 8.0
- **Cache**: Redis 7.0
- **ORM**: Spring Data JPA, Flyway
- **Security**: Spring Security, JWT, OAuth2 (Google, Kakao)
- **Logging**: Logback, Grafana, Loki
- **Infra**: Docker, GitHub Actions CI
- **Docs**: Swagger (springdoc-openapi)

## 주요 기능

### 인증
- 일반 회원가입/로그인 (이메일 + 비밀번호)
- OAuth2 소셜 로그인 (Google, Kakao)
- JWT Access Token / Refresh Token 발급
- 토큰 재발급 및 로그아웃

### 파티
- 파티 생성 (구독 플랜 선택 필수)
- 초대 코드로 파티 조회
- 초대 코드로 파티 참여
- 자동 매칭 파티 참여 (Redis 대기 큐)
- 파티 탈퇴 (즉시 탈퇴 / 탈퇴 예약)

### 정산
- 정산 실행 (NORMAL / REVERSAL / ADJUSTMENT)
- 멱등성 보장
- 비관락 동시성 제어

## 아키텍처

```
Controller → Business → Service → Repository
```

- **Controller**: 요청/응답 처리
- **Business**: 비즈니스 로직 조합
- **Service**: 도메인별 단일 책임 처리
- **Repository**: DB 접근

## API 문서

서버 실행 후 Swagger UI 접속
```
http://localhost:8080/swagger-ui/index.html
```

## 실행 방법

**1. 환경 변수 설정**

프로젝트 루트에 `.env` 파일 생성

```
MYSQL_ROOT_PASSWORD=
MYSQL_DATABASE=
MYSQL_USER=
MYSQL_PASSWORD=
GRAFANA_USER=
GRAFANA_PASSWORD=
```

**2. application.yml 설정**

`src/main/resources/application.yml` 생성 후 아래 내용 작성

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3308/{DB명}?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username:
    password:

  security:
    oauth2:
      client:
        registration:
          google:
            client-id:
            client-secret:
            scope:
              - email
              - profile
          kakao:
            client-id:
            client-secret:
            client-authentication-method: client_secret_post
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/kakao"
            scope:
              - profile_nickname
        provider:
          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id

  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret:
  access-token-expiration: 3600000
  refresh-token-expiration: 604800000
```

**3. Docker 실행**
```bash
docker-compose up -d
```

**4. 서버 실행**
```bash
./gradlew bootRun
```

## 모니터링

Grafana + Loki로 로그 모니터링 가능

```
http://localhost:3000
```