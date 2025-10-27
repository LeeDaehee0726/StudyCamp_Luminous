studtquest : luminous v9은 아래 내용을 업데이트만 하면서 시스템 설정을 관리할거야.


# 📘 StudyQuest : Luminous V9 — 통합 기획

## 1) 핵심 목표

* 개인 학습 관리(V8) → **커뮤니티 학습 플랫폼(V9)** 으로 확장
* 기능 축: 계획/타이머/AI 코칭/칭호/명언 + **OX퀴즈 공유** + **실시간 채팅** + **정답 통계/랭킹**

## 2) 주요 기능 (요약)

* 📅 계획/퀘스트/반복/휴식일 자동 재분배(기존 유지)
* ⏱ 집중 타이머(중단 사유 기록, 분석)
* 🤖 AI 루미 코치(Claude/Gemini) — 개인 피드백/루틴 추천
* 🏷 칭호 & 명언 — D-Day 상단 동기부여
* 🧠 **OX 퀴즈**: 사용자 업로드/풀기/피드백(정답률·난이도), AI 간단 검수
* 💬 **채팅방**: 과목/시험/챌린지별 실시간 대화, 퀴즈 공유/투표
* 📈 **정답 통계 & 랭킹**: 개인 정확도·카테고리 약점, 주/월 랭킹
* 🪙 루미 포인트: 학습/참여 보상 → 테마/코칭/챌린지
* 💳 **구독 단일 플랜**(₩4,900/월): 광고 제거 + 무제한 AI 코칭(정책 한도), 고급 통계, 챌린지 생성권 등
* 📱 홈 위젯(오늘 퀘스트/타이머/주간 진행)

---

# 🧩 V9 시스템 아키텍처 (텍스트)

```
[ Android App (Kotlin, Jetpack Compose, Glance) ]
   |  REST/HTTPS + (채팅은 WebSocket/GraphQL Subscriptions)
   v
[ API Gateway / Load Balancer ]
   |
   +-- Auth Service (Cognito/JWT)
   +-- User Service
   +-- Plan/Quest Service
   +-- Focus Service
   +-- Subscription Service (Google Play 검증 Webhook)
   +-- Quiz Service (OX 업로드/풀이/평가)
   +-- Chat Service (실시간 메시지)
   +-- Stats Service (정답률/랭킹/리포트)
   +-- AI Proxy Service (Claude/Gemini)  ← 프롬프트/요금/캐시
   |
   +-- S3 (이미지/퀴즈 첨부) / CDN
   +-- DB: DynamoDB(또는 Postgres) + Redis Cache
   +-- Realtime: AppSync(GraphQL) or Firebase Realtime DB
   +-- Analytics Warehouse: Athena/Redshift/BigQuery (배치 집계)
   +-- Monitoring: CloudWatch + Sentry/Datadog
```

### 핵심 설계 포인트

* **Local-first**: 계획/타이머/위젯은 로컬(Room) 우선, 서버 동기화는 백그라운드.
* **Server-required**: 인증/구독검증/AI중계/커뮤니티(퀴즈·채팅·랭킹)는 서버 필수.
* **AI 프록시 레이어**: 모델 교체 용이, 입력 데이터 요약·PII 제거, 응답 캐시로 비용절감.
* **실시간 채팅**: AppSync(FaaS) 또는 Firebase Realtime DB로 초기 비용 최소화.
* **집계/랭킹**: 이벤트 스트림 → 야간 배치(서버리스 크론)로 계산 → 캐시.

---

# 🗃 데이터 모델(핵심)

## Users

* userId, email, name/nickname, age/gender/phone(opt), theme/font
* isSubscribed, subscribeUntil, lumiPoints
* titlesOwned[], currentTitle

## Goals / Quests / FocusRecords *(V8 동일 기반)*

* Goals: repeatCount, restDays[], colorTag…
* Quests: round, date, startIndex/endIndex, completed…
* FocusRecords: sessions[{start,end,interruptions}], totalMinutes

## Quizzes (OX)

* quizId, authorId, question, answer(bool), category, difficulty, refImage(optional S3), likes, correctRate
* **QuizResults**: (PK: userId+quizId) isCorrect, solvedAt

## Chat

* chatRoomId (subject/exam/challenge), members[], lastMessageAt
* chatMessageId, roomId, senderId, text|quizRef, createdAt

## Stats

* **UserStats**: totalSolved, correct, accuracy, topCategories[], recentMistakes[]
* **Leaderboards**: period(week/month), metric(score/accuracy), topN[]

---

# 🌐 API(요약 목록)

* **Auth**: `/auth/signup`, `/auth/login`, `/auth/refresh`
* **User**: `/user/profile` (GET/PUT)
* **Plan/Quest**: `/goals`(POST/PUT/GET), `/quests?date=…` (GET), `/quests/{id}/complete` (PUT)
* **Focus**: `/focus/session/start|stop`, `/focus/report?range=7d`
* **Subscription**: `/subscription/verify`(앱 영수증 검증), `/subscription/webhook`(서버용)
* **Quiz**:

  * `POST /quiz` (질문/정답/카테고리/이미지 업로드)
  * `GET /quiz?category=…&sort=popular`
  * `POST /quiz/{id}/solve` ({isCorrect})
  * `GET /quiz/stats/user/{userId}`
* **Chat**:

  * REST: `/chat/rooms`, `/chat/rooms/{id}/join`
  * Realtime: GraphQL `subscribeMessages(roomId)`, `sendMessage(roomId, text|quizRef)`
* **Stats**: `/stats/user/{id}`, `/stats/leaderboard?period=month`
* **AI Proxy**: `/ai/coach/daily`, `/ai/quiz/review`, `/ai/plan/recommend`

---

# 🧱 코드 폴더 아키텍처(요약)

```
studyquest/
├─ app/                            # Android
│  └─ src/main/java/com/studyquest/
│     ├─ ui/ (home/quest/focus/coach/subscription/community)
│     ├─ data/ (models, network, local Room)
│     ├─ repository/
│     ├─ ai/ AiClient.kt
│     ├─ widget/ (Glance)
│     └─ ads/, auth/, util/
├─ backend/
│  ├─ services/
│  │  ├─ auth-service/
│  │  ├─ subscription-service/
│  │  ├─ plan-service/
│  │  ├─ focus-service/
│  │  ├─ quiz-service/
│  │  ├─ chat-service/             # AppSync/Firebase 어댑터 포함
│  │  ├─ stats-service/            # 배치/집계 람다
│  │  └─ ai-proxy-service/         # Claude/Gemini 래퍼 + 프롬프트
│  └─ libs/ (db, cache, s3, utils)
├─ ai/ (prompt-templates, analysis/)
├─ infra/ (terraform or serverless.yml)
├─ docs/ (ARCHITECTURE.md, API_SPEC.md, PROMPTS.md, UX_UI_GUIDELINES.md)
└─ devops/ (.github/workflows CI/CD)
```

---

# 🔁 “코덱스와 계속 업데이트하는” 아키텍처 기반 작업 시스템

## 작업 원칙 (핵심)

1. **날짜 로그 누적 금지** → 토큰 낭비 유발
2. **두 문서가 진실의 원천(Single Source of Truth)**

   * `docs/ARCHITECTURE.md` **(시스템 아키텍처)**
   * `docs/FOLDER_STRUCTURE.md` **(폴더/모듈 구조)**
3. 변경은 항상 **추가/수정/삭제(ADD/UPDATE/REMOVE)** 형태로만 기술
4. 코덱스 입력 시, 두 문서를 먼저 제공하고 “해당 스코프만 수정”을 명령

## 문서 템플릿 예시

**`docs/ARCHITECTURE.md` (발췌 템플릿)**

```md
# System Architecture — v9.0 (2025-10-25)
- Frontend(Android): Compose/Glance, Room local-first
- Backend(Serverless): API Gateway + Lambda + DynamoDB + S3
- Realtime: AppSync (GraphQL)
- AI Proxy: Claude/Gemini, PII 제거 + 요약 입력 + 응답 캐시

## Changes (v9.1 계획)
[ADD] Quiz Service: POST /quiz/{id}/solve → 정답 기록 저장
[UPDATE] Stats Service: nightly job에서 user accuracy 계산에 최근 30일 가중치 도입
[REMOVE] 커뮤니티 파일 업로드의 임시 로컬 저장 로직
```

**`docs/FOLDER_STRUCTURE.md` (발췌 템플릿)**

```md
# Folder Structure — v9.0
backend/services/
  quiz-service/
    createQuiz.js
    listQuizzes.js
    solveQuiz.js  # NEW in v9.1
  stats-service/
    nightlyAggregation.js  # UPDATED in v9.1 (가중치 계산)

app/src/.../ui/community/
  QuizUploadScreen.kt
  QuizPlayScreen.kt
```

## 코덱스 지시 프롬프트(고정 머리말)

```
다음 두 파일(ARCHITECTURE.md, FOLDER_STRUCTURE.md)을 기준으로만 변경한다.
명시된 [ADD]/[UPDATE]/[REMOVE] 스코프 외의 코드는 수정하지 않는다.
변경 후, 영향받는 테스트도 함께 갱신한다.
```

## 작업 단위(스프린트 미니 사이클)

* 1. `Changes` 섹션 편집 → 2) 코덱스에 “이 변경만 반영” 지시 → 3) PR 생성 → 4) CI → 5) 태그(v9.1)

---

# 🔐 보안/비용/운영 가이드(요약)

* **AI 비용 절감**: 입력 데이터 서버 요약 → 짧은 프롬프트, 하루 1회 기본/추가 호출은 프리미엄만
* **개인정보 보호**: PII 토큰화/부분 마스킹, AI 전송 전 제거
* **채팅 안전**: 욕설/도배 필터(람다 전처리), 신고/차단, 메시지 보존 기간 정책
* **성능**: 인기 퀴즈/랭킹 Redis 캐시, 채팅은 FaaS 실시간 서비스 활용
* **모니터링**: AI 호출비·구독 전환·Retention 대시보드

---

# 🚀 V8 → V9 마이그레이션 체크리스트

* [ ] DB: `quizzes`, `quiz_results`, `chat_rooms`, `chat_messages`, `leaderboards` 테이블/파티션 생성
* [ ] API: quiz/chat/stats 라우트 추가, 권한(구독자 특전) 미들웨어
* [ ] 앱: Community 탭, Quiz 업로드/풀이 화면, Chat 화면 추가
* [ ] AI Proxy: `ai/quiz/review` 프롬프트 템플릿 추가
* [ ] 배치: 야간 집계 람다 + 스케줄러(EventBridge)
* [ ] CI: 새로운 서비스 빌드·테스트 파이프라인 추가

---
