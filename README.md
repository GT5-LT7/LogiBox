# 🚚 LogiBox

## 📑 목차
1. [📢 프로젝트 개요](#1-프로젝트-개요)
2. [🎬 주요 기능](#2-주요-기능)
3. [⚙️ 개발 환경](#3-개발-환경)
4. [🛠 기술 스택](#4-기술-스택)
5. [🏗 아키텍처](#5-아키텍처)
6. [📂 패키지 구조](#6-패키지-구조)
7. [💻 로컬 실행 방법](#7-로컬-실행-방법)
8. [🔗 팀 노션 링크](#8-팀-노션-링크)
9. [👥 팀 구성](#9-팀-구성)

---

## 1️⃣ 프로젝트 개요
`LogiBox`는 전국 허브 네트워크를 기반으로 물류 이동 과정을 효율적으로 관리하기 위한 **MSA 기반 국내 물류 관리 및 배송 시스템**입니다.
공급 업체가 속한 허브에서 주문처의 허브까지 물품을 허브 간 이동시키고 최종 목적지까지 안전하게 전달하는 전체 배송 프로세스를 관리합니다. 또한 주문 생성부터 허브 이동, 배송 담당자 배정, 배송 상태 추적까지의 전 과정을 서비스별로 분리하여 안정적이고 확장 가능한 구조로 구현했습니다.

---

## 2️⃣ 주요 기능

### 👤 사용자 및 권한 관리
* 사용자 회원가입 및 로그인
* 사용자 역할(Role) 기반 권한 관리
* 관리자 승인 프로세스 지원
* JWT 기반 인증/인가 처리

### 🏢 허브 및 인프라 관리
* 허브 정보 등록 및 관리
* 허브 간 이동 경로 및 거리 정보 관리
* Redis 캐싱을 통한 경로 조회 성능 최적화
* 허브 기반 물류 이동 흐름 처리

### 🏪 업체 및 상품 관리
* 공급 업체 및 수령 업체 관리
* 상품 등록 및 재고 정보 관리
* 업체별 상품 조회 기능 제공

### 📦 주문 관리
* 주문 생성 및 상태 관리
* 주문 요청 시 배송 서비스와 연동
* RabbitMQ 기반 이벤트 발행 처리
* 주문-배송 프로세스 자동화

### 🚚 배송 및 담당자 관리
* 배송 담당자 자동 배정 로직 처리
* 배송 상태 추적 및 변경 이력 관리
* 허브 이동 및 최종 배송 흐름 관리
* 배송 완료 여부 실시간 반영

### 🔔 알림 및 AI 분석
* 배송 상태 기반 Slack 알림 발송
* AI 기반 배송 데이터 분석 지원
* 이벤트 기반 알림 시스템 구성

---

## 3️⃣ 개발 환경

### 📝 Workflow & Communication
- <img src="https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=white"> : 회의록, API 명세서, ERD, 진행 상황, 규칙, 일정
- <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white"> : 코드 관리 및 이슈 트래킹
    - **Branch 전략:** `main` / `develop` (통합) / `feature/*` (기능 개발) / `fix/*` (버그 수정) / `refactor/*` / `chore/*` / `test/*`
    - **Issue & PR:** GitHub Issue 생성 → 해당 이슈 번호 포함한 PR 작성 → 팀원·CodeRabbit AI 리뷰 후 머지
- <img src="https://img.shields.io/badge/slack-4A154B?style=for-the-badge&logo=slack&logoColor=white"> : 실시간 커뮤니케이션, 이벤트 알림

### ⚙️ Environment Variables
- **`.env.example`** : 팀 공통 환경 변수 템플릿 (Git 추적)
- **`.env`** : 로컬 실제 값 (`.gitignore` 처리됨)
- **`application.yml`** : `${VAR:기본값}` fallback 패턴으로 로컬·Docker 환경 모두 호환
    - `application.yml` : 운영/Docker 환경
    - `src/test/resources/application.yml` : 테스트 환경 (Testcontainers PostgreSQL 16)
- **Docker Compose:** `docker-compose.yml`이 `.env` 파일을 읽어 컨테이너에 환경 변수 주입

### 🧪 CI
- <img src="https://img.shields.io/badge/github_actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white"> : 변경된 모듈만 감지하여 빌드·테스트·Docker 검증

---

## 4️⃣ 기술 스택

### Backend

* Java 17
* Spring Boot 3.5.14
* Spring Data JPA
* QueryDSL

### MSA & Infra

* Netflix Eureka
* Spring Cloud Gateway
* OpenFeign
* RabbitMQ
* Redis
* Docker & Docker Compose

### Database

* PostgreSQL

### Test

* JUnit5
* Mockito
* AssertJ
* Testcontainers

### External API

* Kakao Local / Kakao Mobility (Feign Client)

---

## 5️⃣ 아키텍처
![upload_d444449e90844ae9f9dffd07ee78b8d4](https://hackmd.io/_uploads/rJkkOD8lfe.png)


- **MSA (Microservice Architecture)**
    - `eureka-server`: 서비스 디스커버리
    - `api-gateway`: Spring Cloud Gateway
    - `user-service` / `catalog-service` / `order-service` / `logistics-service`
- **Module:** `module` 공통 모듈 (Security, BaseEntity, 공통 예외 등)
- **DDD 계층형 구조:** `presentation → application → domain → infrastructure`

---

## 6️⃣ 패키지 구조
```text
LogiBox
├── module     # 공통 모듈
│       ├── advice 
│       ├── config 
│       ├── dto    
│       ├── entity 
│       ├── exception 
│       ├── resolver                         
│       └── security 
│
├── infrastructure     # 인프라 서비스
│   ├── eureka-server 
│   │   └── com.spatra.gt5lt7.eureka
│   │
│   └── api-gateway
│       └── com.spatra.gt5lt7.gateway
│           └── filter 
│
└── services     # 도메인 마이크로서비스
    ├── user-service     # User / 인증 / 인가 (JWT)
    │   └── com.spatra.gt5lt7.user
    │
    ├── catalog-service      # Company / Product / KakaoMap
    │   └── com.sparta.gt5lt7.catalog
    │       ├── application
    │       │   ├── facade       
    │       │   └── service                  
    │       ├── domain
    │       │   ├── entity              
    │       │   └── repository  
    │       ├── infrastructure
    │       │   └── client     
    │       │       └── dto
    │       ├── presentation
    │       │   ├── controller          
    │       │   └── dto (request / response)
    │       └── global 
    │
    ├── order-service     # Order / AI / Slack Notification / RabbitMQ
    │   └── com.sparta.gt5lt7.order
    │       ├── application
    │       │   ├── consumer                 
    │       │   ├── event          
    │       │   ├── service            
    │       ├── domain
    │       │   ├── entity   
    │       │   └── repository
    │       ├── infrastructure
    │       │   ├── ai     
    │       │   ├── client        
    │       │   ├── config  
    │       │   ├── lock 
    │       │   ├── messaging    
    │       │   └── slack   
    │       │       └── dto
    │       ├── presentation
    │       │   ├── controller
    │       │   └── dto (request / response)
    │       └── common.exception  
    │
    └── logistics-service      # Hub / HubRoute / Delivery / DeliveryAgent
        └── com.sparta.gt5lt7.logisticsservice
            ├── application
            │   ├── consumer              
            │   ├── event   
            │   ├── service                
            │   ├── dto
            │   ├── HubService
            │   ├── HubRouteService
            │   └── DeliveryAgentService
            ├── domain
            │   ├── entity                   
            │   │   └── type                
            │   └── repository           
            ├── infrastructure
            │   ├── client.kakao            
            │   │   └── dto
            │   └── seed 
            ├── presentation
            │   ├── controller     
            │   └── dto (request / response)
            └── global    
```

### 📦 공통 패키지 구조 (DDD 계층형)

- **`presentation`** : REST API (Controller, Request/Response DTO)
- **`application`** : 비즈니스 로직 (Service, Facade), 메시지 Consumer/Event
- **`domain`** : 도메인 모델 (Entity, Repository)
- **`infrastructure`** : 외부 시스템 연동 (Feign Client, Messaging, Seed)
- **`global`** (또는 `common`) : 서비스별 공통 설정 / 예외 / 유틸

---

## 7️⃣ 로컬 실행 방법
```bash
# 1. 환경 변수 파일 준비
cp .env.example .env
# .env 파일에 실제 값 입력 (DB 계정, JWT 키, Kakao API Key 등)

# 2. 전체 스택 실행
docker compose up -d

# 3. 특정 서비스만 IDE에서 직접 실행할 경우 (DB·Redis·RabbitMQ만 Docker로)
docker compose up logistics-db redis rabbitmq eureka-server -d
./gradlew :services:logistics-service:bootRun
```

---

## 8️⃣ 팀 노션 링크
https://www.notion.so/teamsparta/6-5-7-3562dc3ef5148068a465c9a29722388e

---

## 9️⃣ 팀 구성
| 이름  | 담당 역할 | GitHub |
| --- | ------------------------ | ----------------------------- |
| 강동민 | • Eureka Server + API Gateway<br>• User Service | [@DONGMIN-777](https://github.com/DONGMIN-777) |
| 강다연 | • Logistics Service (Hub + Hub Route + Delivery Manager)  | [@181108-cherry](https://github.com/181108-cherry) |
| 최리아 | • Order Service (Order + Slack + AI)<br>• Logistics Service (Delivery)<br>• RabbitMQ |[@riiach](https://github.com/riiach) |
| 맹현지 | • CI + 공통 모듈 + Docker Compose<br>• Catalog Service (Company + Product) | [@gray-ji](https://github.com/gray-ji) |




