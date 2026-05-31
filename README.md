# guessTheNumber — CI/CD 실습 (Jenkins)

숫자 맞추기 게임(**NUM BREACH**)을 소재로 한 **GitHub + Jenkins CI/CD 파이프라인** 실습 저장소.
코드를 push하면 Jenkins가 자동으로 **빌드 → 테스트 → 배포 → 이메일 알림**까지 수행한다.

---

## 1. 전체 아키텍처

```
[개발자 PC] ──git push──▶ [GitHub: SW-prac/guessTheNumber]
                                   │  webhook (push 이벤트)
                                   ▼
                          [ngrok 터널] ── 외부→로컬 연결
                                   ▼
                ┌──────────────────────────────────────┐
                │  Jenkins (로컬 Docker 컨테이너)         │
                │                                        │
                │  ① Checkout : 소스 받기                │
                │  ② Prepare  : JUnit jar 다운로드        │
                │  ③ Build    : javac 컴파일             │
                │  ④ Test     : JUnit 14개 테스트         │  ← 실패 시 여기서 중단
                │  ⑤ Deploy   : EC2로 scp (테스트 통과 시)│
                │  ⑥ Post     : txt 보관 + 이메일 발송     │
                └──────────────────────────────────────┘
                                   │ scp (SSH)
                                   ▼
                        [AWS EC2  3.36.48.154]
```

> **CI 게이트:** Java/JUnit 테스트가 통과해야만 Deploy 단계로 넘어간다. 불량 코드는 배포되지 않는다.

---

## 2. 역할 분담 (계정별)

세 명이 각자 다른 GitHub 신원으로 영역을 나눠 작업했다.

| 담당 | GitHub 계정 | 역할 | 한 일 |
|------|-------------|------|-------|
| 소스코드 | **김양오** (`diddh789@gmail.com`) | 빌드/테스트 대상 코드 | 게임 로직을 **Java + JUnit5** 로 포팅. `game/` 패키지(`NumberGuessingGame`, `Difficulty`, `GuessResult`, `BestScore`)와 **단위 테스트 14개** 작성. → Jenkins가 컴파일·테스트하는 대상 |
| Docker 세팅 | **최인태** (`show8621@naver.com`) | CI 실행 환경 | `guessNumberDocker/` 의 **docker-compose** 작성 — Jenkins + nGrinder(controller/agent) + ngrok 컨테이너 구성. ngrok 토큰은 `.env`(미커밋)로 분리 |
| Jenkins / 인스턴스 / CD | **박보성** (`alskdj7879@gmail.com`) | 파이프라인 & 배포 | **Jenkinsfile**(파이프라인 정의) 작성, Jenkins Job·Credentials·SMTP·**GitHub webhook** 설정, **EC2 배포(Deploy 단계)** 연결 |

---

## 3. 디렉토리 구조

```
guessTheNumber/
├── Jenkinsfile                 # CI/CD 파이프라인 정의 (박보성)
├── game/                       # 빌드/테스트 대상 Java 코드 (yahofarewell)
│   ├── src/game/
│   │   ├── Difficulty.java          # 난이도 enum (EASY/NORMAL/HARD)
│   │   ├── GuessResult.java         # 판정 결과 enum
│   │   ├── NumberGuessingGame.java  # 핵심 로직 (guess 판정, 시도 카운트)
│   │   ├── BestScore.java           # 최고기록 갱신
│   │   └── *Test.java               # JUnit5 테스트 (14개)
│   └── README.md
├── guessNumberDocker/          # Jenkins 실행용 Docker 구성 (andychoi21)
│   ├── docker-compose.yml           # Jenkins + nGrinder + ngrok
│   ├── .env.example                 # NGROK_AUTHTOKEN placeholder
│   └── README.md
├── index.html                  # coming-soon 정적 페이지 (CD 배포 대상)
└── docs/                       # 참고 스크린샷
```

> 참고: 실제로 브라우저에서 **플레이되는 게임은 JavaScript**(`number-guessing-game/` 의 `game.js`)이며, 본 저장소의 Java 코드는 **같은 규칙을 옮긴 CI 테스트 대상**이다. (서로 별개 구현)

---

## 4. 파이프라인 단계 (Jenkinsfile)

| 단계 | 내용 |
|------|------|
| Checkout | GitHub에서 소스 체크아웃 |
| Prepare | `classes/`, `test-reports/`, `lib/` 생성 + JUnit console-standalone jar 다운로드 |
| Build | `cd game && javac` 로 `.java` 컴파일 |
| Test | JUnit5 실행 → `test-reports/test-output.txt` 저장 |
| **Deploy** | **테스트 통과 시** `index.html` 을 EC2로 `scp` (자격증명 `ec2-ssh`) |
| post.always | JUnit 리포트 기록 + `archiveArtifacts` (txt 산출물) |
| post.success | **이메일 발송** (`[Jenkins] SUCCESS …`) |
| post.failure | 실패 알림 이메일 |

- 트리거: **GitHub webhook**(push 시 자동 빌드) — `GitHub hook trigger for GITScm polling`
- 알림: Gmail SMTP(`smtp.gmail.com:465`, SSL)

---

## 5. CI 실행 환경 (Docker)

`guessNumberDocker/docker-compose.yml` 로 컨테이너 4개를 띄운다 (`docker compose up -d`).

| 컨테이너 | 역할 | 포트 |
|----------|------|------|
| jenkins | CI/CD 본체 | `localhost:8081` |
| ngrok | 외부→Jenkins 터널 (webhook 수신) | — |
| controller / agent | nGrinder(부하테스트) | `:80`, `12000~` |

![Docker Desktop 컨테이너 목록](docs/docker-containers.png)

---

## 6. Jenkins 파이프라인 실행 결과

push 시 webhook으로 자동 트리거되어 모든 단계가 통과(초록)하는 모습.

![Jenkins 파이프라인 단계](docs/jenkins-pipeline.png)

---

## 7. 접속 정보

| 대상 | 주소 |
|------|------|
| Jenkins 대시보드 | `http://localhost:8081` |
| GitHub 저장소 | `https://github.com/SW-prac/guessTheNumber` |
| 배포 서버 (EC2) | `http://3.36.48.154:8000` |

> ⚠️ ngrok 무료 플랜은 재시작 시 공개 URL이 바뀌므로, 그때마다 GitHub webhook의 Payload URL(`…/github-webhook/`)을 갱신해야 한다.

---

## 8. Mission 충족 요약

- [x] GitHub 저장소 생성 및 협업(역할별 커밋)
- [x] push 시 Jenkins 자동 빌드 (webhook)
- [x] 빌드 실패 시 원인 분석 가능 (Console Output / Stage View)
- [x] 빌드 성공 시 txt 보관 + **이메일 발송**
- [x] (확장) 테스트 통과 시 EC2 자동 배포 (CD)
