# guessTheNumber — CI/CD 실습 (Jenkins)

배포된 게임 주소: http://3.36.48.154:8000

숫자 맞추기 게임(NUM BREACH)을 소재로 한 GitHub + Jenkins CI/CD 파이프라인 실습 저장소다. 코드를 push하면 Jenkins가 빌드, 테스트, 배포, 이메일 알림까지 자동으로 수행한다.

---

## 1. 과제 요구사항(CI)과 실제 구현(CD)

| 구분 | 범위 |
|------|------|
| 과제 요구사항 | CI. push 시 자동 빌드와 테스트, 실패 원인 분석, 성공 시 결과 보관과 이메일 |
| 실제 구현 | CI + CD. 위 모두에 더해 테스트 통과 시 AWS EC2 자동 배포 |

과제에서 요구한 범위는 CI(지속적 통합)까지였다. 즉 코드를 push하면 Jenkins가 자동으로 빌드와 테스트를 수행하고, 실패 시 원인을 파악하며, 성공 시 결과를 보관하고 통지하는 데까지다.

그러나 본 프로젝트는 한 단계 더 나아가 CD(지속적 배포)까지 구현했다. 그 이유는 다음과 같다.

Jenkins는 단순히 빌드를 자동화하는 도구가 아니라, 빌드와 테스트를 거쳐 지속적 통합을 이루고, 이를 실제 인스턴스에 자동으로 반영해 지속적 배포까지 자동화하는 데에 강점을 갖고 있는 파이프라인이다.

따라서 CI/CD 전체를 Jenkins로 구현하여 실습을 진행했다.

---

## 2. 전체 아키텍처

```
[개발자 PC] ──git push──▶ [GitHub: SW-prac/guessTheNumber]
                                   │  webhook (push 이벤트)
                                   ▼
                          [ngrok 터널] ── 외부에서 로컬로 연결
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

테스트가 통과해야만 Deploy 단계로 넘어가므로, 테스트는 배포의 안전장치 역할을 한다. 테스트가 깨지면 불량 코드는 서버에 올라가지 않는다.

---

## 3. 역할 분담

세 명이 각자 다른 GitHub 신원으로 영역을 나눠 작업했다.

| 담당자 (이메일) | 역할 | 한 일 |
|-----------------|------|-------|
| 김양오 (`diddh789@gmail.com`) | 소스코드 (빌드와 테스트 대상) | 게임 로직을 Java와 JUnit5로 포팅했다. `game/` 패키지(`NumberGuessingGame`, `Difficulty`, `GuessResult`, `BestScore`)와 단위 테스트 14개를 작성했고, Jenkins가 이를 컴파일하고 테스트한다. |
| 최인태 (`show8621@naver.com`) | Docker 세팅 (CI 실행 환경) | `guessNumberDocker/` 의 `docker-compose`를 작성했다. Jenkins, nGrinder(controller, agent), ngrok 컨테이너를 구성했고, ngrok 토큰은 커밋하지 않는 `.env`로 분리했다. |
| 박보성 (`alskdj7879@gmail.com`) | Jenkins, 인스턴스, CD | `Jenkinsfile`(파이프라인 정의)을 작성하고, Jenkins Job과 Credentials, SMTP, GitHub webhook을 설정했으며, EC2 배포(Deploy 단계)를 연결했다. |

---

## 4. 디렉토리 구조

```
guessTheNumber/
├── Jenkinsfile                 # CI/CD 파이프라인 정의 (박보성)
├── game/                       # 빌드/테스트 대상 Java 코드 (김양오)
│   ├── src/game/
│   │   ├── Difficulty.java          # 난이도 enum (EASY/NORMAL/HARD)
│   │   ├── GuessResult.java         # 판정 결과 enum
│   │   ├── NumberGuessingGame.java  # 핵심 로직 (guess 판정, 시도 카운트)
│   │   ├── BestScore.java           # 최고기록 갱신
│   │   └── *Test.java               # JUnit5 테스트 (14개)
│   └── README.md
├── guessNumberDocker/          # Jenkins 실행용 Docker 구성 (최인태)
│   ├── docker-compose.yml           # Jenkins + nGrinder + ngrok
│   ├── .env.example                 # NGROK_AUTHTOKEN placeholder
│   └── README.md
├── index.html                  # 정적 페이지 (CD 배포 데모 대상)
└── docs/                       # 참고 스크린샷
```

참고로 실제로 브라우저에서 플레이되는 게임은 JavaScript(`number-guessing-game/` 의 `game.js`)이고, 본 저장소의 Java 코드는 같은 규칙을 옮긴 CI 테스트 대상이다. 둘은 별개 구현이다.

---

## 5. 파이프라인 단계 (Jenkinsfile)

| 단계 | 내용 |
|------|------|
| Checkout | GitHub에서 소스 체크아웃 |
| Prepare | `classes/`, `test-reports/`, `lib/` 생성 후 JUnit console-standalone jar 다운로드 |
| Build | `cd game && javac` 로 `.java` 컴파일 |
| Test | JUnit5 실행 후 결과를 `test-reports/test-output.txt`에 저장 |
| Deploy | 테스트 통과 시 정적 파일을 EC2로 `scp` (자격증명 `ec2-ssh`) |
| post.always | JUnit 리포트 기록과 `archiveArtifacts` (txt 산출물) |
| post.success | 이메일 발송 (`[Jenkins] SUCCESS …`) |
| post.failure | 실패 알림 이메일 |

트리거는 GitHub webhook으로, push 시 자동으로 빌드된다 (`GitHub hook trigger for GITScm polling`). 알림은 Gmail SMTP를 사용한다 (`smtp.gmail.com:465`, SSL).

---

## 6. CI 실행 환경 (Docker)

`guessNumberDocker/docker-compose.yml` 로 컨테이너 4개를 띄운다 (`docker compose up -d`).

| 컨테이너 | 역할 | 포트 |
|----------|------|------|
| jenkins | CI/CD 본체 | `localhost:8081` |
| ngrok | 외부에서 Jenkins로 들어오는 터널 (webhook 수신) | 내부 전용 |
| controller, agent | nGrinder(부하테스트) | `:80`, `12000~` |

![Docker Desktop 컨테이너 목록](docs/docker-containers.png)

---

## 7. Jenkins 파이프라인 실행 결과

push 시 webhook으로 자동 트리거되어 모든 단계가 통과(초록)하는 모습.

![Jenkins 파이프라인 단계](docs/jenkins-pipeline.png)

---

## 8. 접속 정보

| 대상 | 주소 |
|------|------|
| 배포 서버 (게임) | http://3.36.48.154:8000 |
| Jenkins 대시보드 | `http://localhost:8081` |
| GitHub 저장소 | `https://github.com/SW-prac/guessTheNumber` |

ngrok 무료 플랜은 재시작할 때마다 공개 URL이 바뀌므로, 그때마다 GitHub webhook의 Payload URL(`…/github-webhook/`)을 갱신해야 한다.

---

## 9. Nginx, TLS, DNS를 적용하지 않은 이유

배포 서버는 Python 내장 `http.server`로 정적 파일을 IP와 평문 HTTP로 서빙한다. 운영 환경에서 흔히 더하는 Nginx, TLS, DNS는 다음 이유로 적용하지 않았다.

Nginx(웹서버 겸 리버스 프록시)는 단일 인스턴스에서 트래픽이 적은 정적 파일을 서빙하는 이 실습 환경에서는 `http.server`만으로 충분하다. 리버스 프록시나 로드밸런싱, 캐싱, 높은 동시성 처리 같은 Nginx의 강점은 운영 규모에서 의미가 크지만, 이 프로젝트의 핵심인 CI/CD 파이프라인 흐름과는 직접 관련이 없어 구성을 단순하게 유지했다. 운영으로 전환할 때 서빙 계층만 Nginx로 교체하면 된다.

TLS(HTTPS)는 신뢰할 수 있는 인증서를 발급받으려면 보통 도메인이 먼저 있어야 한다(Let's Encrypt 등). 이 실습은 IP로 직접 접속하는 데모이고 민감한 정보를 주고받지 않으므로 평문 HTTP로 충분하다고 판단했다. 도메인을 확보한 뒤 `certbot` 등으로 어렵지 않게 추가할 수 있어 범위에서 제외했다.

DNS(도메인)는 등록에 비용이 들고, 도메인 자체는 IP를 기억하기 쉬운 이름으로 바꿔주는 편의 기능일 뿐 CI/CD 동작과는 무관하다. 학습 목적에는 IP 직접 접속으로 충분해 생략했다.

세 가지 모두 운영 품질을 높이는 요소이지 CI/CD 파이프라인의 본질은 아니므로, 실습의 초점을 흐리지 않기 위해 범위에서 제외했다.
