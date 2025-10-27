# 📁 FOLDER_STRUCTURE.md
## StudyQuest : Luminous V9 (2025-10-25)

본 문서는 StudyQuest 앱의 실제 코드 구조를 정의하며,  
모든 코드 생성·수정 시 이 구조를 기준으로 한다.  
코드 변경은 [ADD], [UPDATE], [REMOVE] 형식으로만 기술한다.

---

## 1️⃣ app/ — Android Client (Jetpack Compose + Glance)
### 구조
app/
└─ src/main/java/com/studyquest/
├─ ui/ # 화면 구성
│ ├─ home/ # 홈화면(D-day, 명언, 목표, 루미포인트)
│ ├─ quest/ # 퀘스트 목록, 완료 처리
│ ├─ focus/ # 집중 타이머, 중단사유 기록
│ ├─ coach/ # AI 루미 코치 피드백
│ ├─ subscription/ # 구독 결제/혜택 안내
│ ├─ community/ # OX퀴즈, 채팅, 랭킹
│ └─ common/ # 공통 컴포넌트(버튼, 카드뷰)
│
├─ data/ # 데이터 계층
│ ├─ models/ # User, Goal, Quest, FocusRecord, Quiz 등
│ ├─ network/ # ApiService, ApiClient
│ └─ local/ # Room DB, DAO, 엔티티
│
├─ repository/ # 데이터 통합 접근 계층
├─ ai/ # AI 호출 래퍼(Claude/Gemini)
├─ widget/ # Glance 위젯 (오늘의 퀘스트, 타이머)
├─ ads/ # 배너 광고 관리
├─ auth/ # 로그인/회원가입 관리
└─ util/ # 날짜, 문자열, 테마 유틸

yaml
코드 복사

### 참고
- 모든 API 호출은 `ApiClient` → `Repository` → `ViewModel` → `UI` 순서로 흐른다.
- Room DB는 오프라인 모드 지원용으로 사용된다.

---

## 2️⃣ backend/ — Serverless Backend (AWS Lambda 기반)
### 구조
backend/
├─ services/
│ ├─ auth-service/ # 로그인, JWT, Cognito 연동
│ ├─ subscription-service/ # 구독 검증 및 Webhook
│ ├─ plan-service/ # 목표 생성/재분배, 휴식일 반영
│ ├─ focus-service/ # 집중 세션 저장, 중단 사유 처리
│ ├─ quiz-service/ # OX 퀴즈 업로드/풀이/AI 검수
│ ├─ chat-service/ # 실시간 채팅 (AppSync/Firebase)
│ ├─ stats-service/ # 통계/랭킹 배치
│ └─ ai-proxy-service/ # Claude/Gemini API 중계
│
├─ libs/ # 공용 라이브러리
│ ├─ db.js # DynamoDB/RDS 연결
│ ├─ s3.js # 이미지 업로드
│ ├─ cache.js # Redis 캐시
│ └─ utils.js # 공통 함수
│
├─ package.json
├─ serverless.yml or terraform/ # 배포 정의
└─ tests/ # 백엔드 유닛 테스트

yaml
코드 복사

---

## 3️⃣ ai/ — AI Layer
ai/
├─ prompt-templates/ # Claude/Gemini 프롬프트 JSON
│ ├─ coach_daily.json # 일일 피드백
│ ├─ plan_recommend.json # 공부 계획 추천
│ └─ quiz_review.json # 퀴즈 문항 검수/피드백
│
├─ analysis/ # 데이터 분석/요약 스크립트
│ └─ focus_analysis.py
│
└─ tests/

yaml
코드 복사

---

## 4️⃣ infra/ — Infrastructure & Deployment
infra/
├─ terraform/ # AWS 리소스 정의
│ ├─ main.tf
│ ├─ variables.tf
│ └─ outputs.tf
├─ serverless.yml # Lambda 서비스 배포 정의
└─ cloudformation/ # 선택적 CloudFormation 설정

yaml
코드 복사

---

## 5️⃣ docs/ — Documentation
docs/
├─ ARCHITECTURE.md # 시스템 아키텍처
├─ FOLDER_STRUCTURE.md # 폴더 구조 (본 문서)
├─ API_SPEC.md # API 명세
├─ PROMPTS.md # AI 프롬프트 설명
└─ UX_UI_GUIDELINES.md # UI/디자인 톤 가이드

yaml
코드 복사

---

## 6️⃣ devops/ — CI/CD & Automation
devops/
├─ .github/workflows/ # GitHub Actions
│ ├─ ci.yml # 빌드/테스트
│ └─ cd.yml # 배포 자동화
├─ docker/ # Docker 환경
│ └─ Dockerfile.backend
└─ scripts/
├─ deploy.sh
└─ analyze-costs.py

yaml
코드 복사

---

## 7️⃣ tests/
tests/
├─ e2e/ # 통합 E2E 테스트
└─ integration/ # 서비스 간 테스트

yaml
코드 복사

---

## ⚙️ 관리 원칙
- 모든 신규 파일/폴더 생성 시 이 문서에 [ADD]/[UPDATE]/[REMOVE]로 기록.
- 코덱스·클로드·AI 코드 생성기는 이 문서의 구조만 신뢰해야 함.
- 명시되지 않은 위치의 코드는 수정하지 않는다.
- 버전 표기 예시: `v9.1 — 2025-11-02`