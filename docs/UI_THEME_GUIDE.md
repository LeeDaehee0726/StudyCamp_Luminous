좋아요.
이건 실제 UI 개발과 디자인 톤을 일관되게 유지하기 위한 **UI 디자인 가이드 문서(`docs/UI_THEME_GUIDE.md`)** 로 관리하기에 완벽한 내용이에요.

아래는 말씀하신 **“하늘색 기반, 구름 같은 세련된 공부 분위기”** 를 반영한
📘 **StudyQuest : Luminous V9 UI 테마 다이어그램 문서 초안**입니다.
이 문서는 나중에 업데이트하면서 색상, 컴포넌트, 페이지 구성 등을 계속 버전 관리할 수 있도록 설계돼 있어요.

---

```md
# 🎨 UI_THEME_GUIDE.md  
## StudyQuest : Luminous V9 — "Sky & Focus" Theme  
*(2025-10-25 Initial)*  

---

## 🌤️ 전체 테마 컨셉
> "맑은 하늘 아래에서 공부하는 듯한 평온함과 집중감"

- **메인 배경:** 하늘색 + 구름 느낌의 그라데이션  
  `linear-gradient(180deg, #B2E4FA 0%, #EAF8FF 100%)`  
- **포인트 컬러:** 루미 블루 `#6DD5FA`  
- **텍스트:** 진한 네이비 `#1E3A5F` (명확한 대비)  
- **하이라이트:** 골드 `#FFD966` (칭호·루미 포인트 강조)
- **버튼:** 반투명 흰색 → 클릭 시 루미 블루로 라이트 효과  

> 📘 전반적인 인상:  
> 깨끗하고 감성적인 “스터디 다이어리 + 퀘스트 RPG” 느낌  

---

## 📱 UI 구성 다이어그램 (텍스트형)

```

┌───────────────────────────────────────────────┐
│ 🌤 StudyQuest : Luminous                     │
│-----------------------------------------------│
│ 🗓 HOME SCREEN                                │
│  ├─ 상단 : D-day, 오늘의 명언 (흰색 카드, 반투명)
│  │       💬 "작은 꾸준함이 큰 변화를 만든다."
│  ├─ 중앙 : 이번 주 목표 달성률 (하늘색 원형 그래프)
│  ├─ 하단 : 오늘의 퀘스트 리스트 (카드형)
│  │       - ‘생명과학 30p 읽기’
│  │       - ‘단어 20개 복습’
│  └─ Footer : [목표추가] [루미포인트] [AI코치] 버튼
│                                               │
│ 배경 : gradient(#B2E4FA → #EAF8FF) + 구름 SVG
│-----------------------------------------------│
│ 🎯 QUEST SCREEN                              │
│  ├─ 상단 : 진행률 바(루미 블루)
│  ├─ 중단 : 오늘 할 일 리스트 (체크박스 카드)
│  └─ 하단 : 완료 시 “✨ 퀘스트 클리어!” 애니메이션
│                                               │
│-----------------------------------------------│
│ ⏱ FOCUS SCREEN                              │
│  ├─ 원형 타이머 (루미 블루 링 애니메이션)
│  ├─ [시작]/[일시정지] 버튼 (반투명 흰색, 그림자효과)
│  ├─ 중단 사유 선택창 : 잠 / 수다 / 핸드폰 / 기타
│  └─ 하단 : 누적 공부시간, 집중도 그래프 (곡선형)
│                                               │
│-----------------------------------------------│
│ 🤖 COACH SCREEN                              │
│  ├─ 오늘의 피드백 카드
│  │   “오늘은 45분 이상 집중하셨네요.
│  │    내일은 같은 시간대에 1세션 더 추천드립니다.”
│  ├─ [루미 리포트 보기] 버튼
│  └─ 하단 : AI 루미 이미지(마스코트)
│                                               │
│-----------------------------------------------│
│ 💳 SUBSCRIPTION SCREEN                       │
│  ├─ 구독 혜택 요약 (아이콘 + 텍스트)
│  │   - 광고 없음
│  │   - AI 코칭 무제한
│  │   - 개인 맞춤 통계
│  ├─ 버튼 : [₩4,900/월 구독하기]
│  └─ 배경 : 루미 블루 → 화이트 부드러운 그라데이션
│                                               │
│-----------------------------------------------│
│ 💬 COMMUNITY SCREEN                          │
│  ├─ 상단 : 탭 (퀴즈 / 채팅 / 랭킹)
│  ├─ [퀴즈 탭]
│  │   - OX 카드 목록 (사용자 이름, 정답률, 좋아요)
│  │   - [퀴즈 만들기] 버튼 (루미 블루 라운드버튼)
│  ├─ [채팅 탭]
│  │   - 말풍선 UI (루미 블루 vs 흰색)
│  │   - 상단 고정 공지: “오늘의 챌린지 시작!”
│  ├─ [랭킹 탭]
│  │   - 주간 TOP10 리스트
│  │   - 사용자 아바타 + 칭호 + 점수
│  └─ 배경 : gradient(#B2E4FA → #EAF8FF)
│                                               │
│-----------------------------------------------│
│ 🌈 COMMON COMPONENTS                          │
│  ├─ Button : radius-xl, shadow-md, 투명 흰색 배경
│  ├─ Card : 둥근 모서리, 흰색 반투명, blur(15px)
│  ├─ ProgressBar : 루미 블루 gradient
│  ├─ Typography :
│      Title — Poppins Bold / #1E3A5F
│      Body — Noto Sans Regular / #2C3E50
│  └─ 아이콘 : Lucide-react / shadcn-ui 스타일 유지
└───────────────────────────────────────────────┘

```

---

## 🎨 색상 팔레트
| 이름 | HEX | 용도 |
|------|------|------|
| Sky Base | `#B2E4FA` | 배경 시작 (상단) |
| Sky Soft | `#EAF8FF` | 배경 끝 (하단) |
| Luminous Blue | `#6DD5FA` | 포인트 컬러 / 버튼 / 진행률 |
| Deep Navy | `#1E3A5F` | 본문 텍스트 |
| White Cloud | `#FFFFFFCC` | 반투명 카드 배경 |
| Gold Accent | `#FFD966` | 칭호/루미포인트 강조 |

---

## 🧭 디자인 톤 & 가이드
- 전체 톤은 **밝고 부드럽게**, 폰트는 라운드 계열 (Poppins, Noto Sans)
- 카드/버튼에 **glassmorphism 효과(blur + 투명도)** 적용
- 아이콘은 **얇고 선형(Lucide 스타일)** 로 통일
- 화면 전환 시 **Framer Motion** 으로 부드러운 페이드/슬라이드
- 구름·햇살 SVG는 최소 15% 투명도로 백그라운드에 위치
- 공부 성취시 ✨ 애니메이션 효과 (별빛/루미 스파크)

---

## 🪶 업데이트 규칙
- UI 변경 시 반드시 이 문서에 [ADD]/[UPDATE]/[REMOVE] 형태로 기록  
- 컬러·폰트·아이콘 등 디자인 토큰 변경 시 명시적 버전 업 (`v9.1 → v9.2`)
- 모든 화면은 동일한 gradient 톤 유지 (Sky Base → Sky Soft)

---

📘 *이 문서는 AI 코드 생성기(코덱스, 클로드 등)에게 UI 일관성을 보장하기 위한 디자인 참조 문서입니다.*
```

---

이걸 `/docs/UI_THEME_GUIDE.md` 로 저장해두면,

* 새로운 UI 화면 추가 시 ([ADD] community/ChallengeScreen 등)
* 색상·폰트 수정 시 ([UPDATE] Luminous Blue → #66D0F9 등)
  이 문서에만 기록해두면 됩니다.

원하신다면 다음 단계로
👉 **이 UI 테마에 맞춘 컴포넌트 구조 (공통 버튼, 카드, 그래프 등)**
을 `common/` 폴더 기준으로 추가 문서(`UI_COMPONENT_GUIDE.md`)로 만들어드릴 수도 있어요.
그것도 같이 구성해드릴까요?


# 🧩 UI_COMPONENT_GUIDE.md  
## StudyQuest : Luminous V9 — 공통 UI 컴포넌트 디자인 가이드  
*(2025-10-25 Initial)*  

---

## 🌈 테마 색상 재사용
| 이름 | HEX | 설명 |
|------|------|------|
| Primary | `#6DD5FA` | 주요 포인트 컬러 (루미 블루) |
| Secondary | `#B2E4FA` | 부드러운 하늘 배경 |
| Background | `linear-gradient(180deg, #B2E4FA 0%, #EAF8FF 100%)` | 전체 화면 배경 |
| TextMain | `#1E3A5F` | 본문 텍스트 |
| CardWhite | `#FFFFFFCC` | 반투명 카드 배경 (blur 적용) |
| AccentGold | `#FFD966` | 강조 요소 (칭호, 포인트) |
| Shadow | `rgba(0,0,0,0.1)` | 그림자 효과 |

---

## 🪶 공통 폰트 / 텍스트 스타일
| 스타일명 | 폰트 | 크기 | 색상 | 용도 |
|-----------|------|------|------|------|
| TitleXL | Poppins Bold | 26sp | `#1E3A5F` | 화면 제목 |
| TitleM | Poppins SemiBold | 20sp | `#1E3A5F` | 카드 제목 |
| Body | Noto Sans Regular | 16sp | `#2C3E50` | 일반 텍스트 |
| Caption | Noto Sans Light | 14sp | `#506680` | 보조 설명문 |
| Quote | Poppins Italic | 18sp | `#1E3A5F` | 명언/동기부여 문구 |

---

## 🧱 기본 컴포넌트 구조

### 🪄 1. Button (루미 버튼)


┌─────────────────────┐
│ [ 시작하기 ▶ ] │
└─────────────────────┘

**속성**
- Radius: `16dp`
- Background: `#FFFFFFAA` (반투명 흰색)
- Shadow: `0px 3px 8px rgba(0,0,0,0.2)`
- Hover/Press: `Primary Gradient (#6DD5FA → #66C8F3)`
- TextColor: `#1E3A5F`
- Animation: Ripple 효과 + Press 시 밝기 10% 증가

**종류**
- `PrimaryButton` : 주요 행동 (시작, 완료, 구독 등)
- `GhostButton` : 테두리만 표시, Secondary 색상
- `IconButton` : 원형 아이콘 (예: +, ▶, ⏸)

---

### 🧊 2. Card (유리효과 카드)


┌────────────────────────────┐
│ 🌤 오늘의 명언 │
│ "작은 꾸준함이 큰 변화를" │
└────────────────────────────┘

**속성**
- Background: `#FFFFFFCC`
- BorderRadius: `20dp`
- Blur: `15px`
- Shadow: `rgba(0,0,0,0.08)`
- 내부 패딩: `16dp`
- Typography: TitleM + Body 조합
- Optional: 아이콘 or 일러스트 우측 배치 가능

---

### ⏱ 3. Progress Bar / Circular Gauge


(원형)
╭────╮
/
│ 75% │
______/

- Linear Mode: Gradient(`Primary → AccentGold`)
- Circular Mode: `strokeWidth: 8dp`, `trailColor: #EAF8FF`
- Animation: 부드러운 EaseInOut
- 텍스트 오버레이: 현재 진행률 %

---

### 📊 4. Info Widget (정보 위젯)
**홈화면 / 위젯용 요약**


┌──────────────────────────┐
│ 📖 오늘 공부시간 1시간45분 │
│ D-day 12일 남음 │
└──────────────────────────┘

- 배경: 반투명 화이트, Blur 적용
- 아이콘: Lucide (책, 시계, 캘린더 등)
- 폰트: TitleM + Caption 조합
- Gradient Overlay: Top에 살짝 하늘색(10%)

---

### 💬 5. Chat Bubble (채팅 풍선)


A: 공부 진짜 열심히 하신다! 💪
B: 오늘 2시간 채웠어요 ☁️

**내 메시지**
- Align: Right  
- Background: `#6DD5FA` (Primary)
- Text: White
- Shadow: Light
- Radius: `16dp 16dp 0dp 16dp`

**상대 메시지**
- Align: Left  
- Background: `#FFFFFFAA`
- Text: `#1E3A5F`
- Radius: `16dp 16dp 16dp 0dp`

**공통**
- Margin: 8dp
- 타임스탬프는 Caption 스타일

---

### 🧠 6. Quiz Card (OX 퀴즈)


┌──────────────────────────┐
│ Q. 광합성은 주로 잎에서 일어난다 │
│ ⭕ ❌ │
└──────────────────────────┘

- 배경: WhiteCard + Gradient Border(`Primary`)
- Question Text: TitleM
- Button: IconButton(O/X)
- Animation: 정답시 파티클 ✨, 오답시 흔들림

---

### 🏆 7. Rank Card (랭킹/칭호)


🥇 1위 | 루미나서
정확도 92% | 포인트 3200

- Layout: Row 정렬  
- 배경: 흰색 카드 + Gold Gradient Border
- Rank Icon: 🥇🥈🥉 or 숫자
- Typography: TitleM + Caption
- ProgressBar(Optional): 정확도 시각화

---

### 🌟 8. Quote Banner (명언 배너)


“공부의 즐거움은 도전의 연속 속에서 피어난다.” ✨

- 배경: 하늘색 그라데이션 (흰 구름 흐름 포함)
- 폰트: Poppins Italic, 20sp
- 아이콘: 🌤 or ✨
- TextColor: `#1E3A5F`
- MarginTop: 12dp

---

### 📦 9. Modal / Dialog (루미 피드백)


────────────────────────
📘 오늘의 루미 피드백
"오늘은 목표 대비 95% 달성했어요!
내일은 오전 집중이 가장 좋을 때네요 ☀️"
[닫기] [AI 코칭 보기]
────────────────────────

- Background: Glass Blur (white 70%)
- Border: `#6DD5FA 1px`
- Button: Primary/Secondary 혼합
- Animation: Fade In + Scale Up (Framer Motion)

---

## 🧭 상단 / 하단 공통 레이아웃
| 영역 | 설명 |
|------|------|
| **TopBar** | 투명 바탕 + 왼쪽 아이콘, 가운데 제목 |
| **BottomNav** | 5탭 (홈 / 퀘스트 / 코치 / 커뮤니티 / 설정), 아이콘 색 변화 애니메이션 |
| **FloatingActionButton** | 루미 블루 + 아이콘 그림자 (퀘스트 추가 등) |

---

## 🎬 전환 애니메이션 가이드
| 전환 | 효과 | 지속시간 |
|------|------|----------|
| 페이지 전환 | Fade + Slide Up | 400ms |
| 카드 등장 | Scale + Fade In | 300ms |
| 버튼 클릭 | Ripple + Light Flash | 200ms |
| 명언 변경 | Crossfade | 800ms |
| 퀴즈 정답 | Spark Particle | 600ms |

---

## 📘 관리 규칙
- UI 관련 변경은 `UI_THEME_GUIDE.md` 와 이 문서 둘 다 업데이트  
- 공통 컴포넌트 생성 시, `common/` 폴더 내 동일한 이름으로 구성  
- 프론트엔드 코드 생성 시 코덱스/클로드는 이 문서 기준으로 스타일 상속  

---

🩵 *이 문서는 StudyQuest의 모든 화면이 “Sky & Focus” 컨셉으로 통일되도록 유지하기 위한 참조 문서입니다.*


이 문서를 /docs/UI_COMPONENT_GUIDE.md 로 넣으면
→ UI_THEME_GUIDE.md(색감/분위기) + UI_COMPONENT_GUIDE.md(형태/구조)
이 두 개로 완전한 디자인 일관성 관리 체계가 완성돼요.