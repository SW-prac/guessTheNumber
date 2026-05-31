# Number Guessing Game (Java/JUnit5 포트)

숫자 맞추기 게임의 핵심 로직을 Java로 옮긴 모듈입니다. Jenkins CI 파이프라인이
`src`를 컴파일하고 JUnit5 테스트를 실행하는 용도로 사용합니다.

## 구성

`src/game/` 패키지 `game`:

- `Difficulty` — 난이도 enum. EASY(1-50/15), NORMAL(1-100/10), HARD(1-200/7).
- `GuessResult` — 추측 결과 enum. INVALID / TOO_LOW / TOO_HIGH / WIN / LOSE.
- `NumberGuessingGame` — 게임 한 판의 상태와 규칙. 범위 밖 입력은 시도 횟수를
  소모하지 않고 INVALID 반환, 정답이면 WIN, 시도 소진 시 LOSE,
  그 외에는 HIGHER/LOWER 힌트(TOO_LOW/TOO_HIGH)를 돌려줍니다.
- `BestScore` — 최소 시도 횟수(베스트 스코어) 기록. 더 적은 시도일 때만 갱신.

## 테스트

- `NumberGuessingGameTest`, `DifficultyTest`, `BestScoreTest` (JUnit5).

## Jenkins 파이프라인

파이프라인은 `src` 아래 `*.java`를 컴파일한 뒤 JUnit5 콘솔 러너로 테스트를
실행하여 전부 통과하는지 확인합니다.
