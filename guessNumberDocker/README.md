# Jenkins / nGrinder / ngrok Docker 설정

이 디렉터리는 CI 실습을 위한 Jenkins, nGrinder, ngrok Docker 구성을 담고 있습니다.
(Apple Silicon / macOS 환경 기준 `docker-compose.yml` 입니다.)

## 구성 서비스

- **controller / agent**: nGrinder 부하 테스트 컨트롤러 및 에이전트
- **jenkins**: Jenkins LTS 서버
- **ngrok**: Jenkins를 외부에 공개하는 터널 (GitHub Webhook 용)

## 사용 방법

1. `.env.example` 파일을 `.env` 로 복사합니다.

   ```bash
   cp .env.example .env
   ```

2. `.env` 파일을 열어 본인의 ngrok authtoken 을 입력합니다.

   ```
   NGROK_AUTHTOKEN=발급받은_본인_토큰
   ```

   > `.env` 파일은 `.gitignore` 에 등록되어 있어 커밋되지 않습니다. 토큰은 절대 저장소에 올리지 마세요.

3. 컨테이너를 실행합니다.

   ```bash
   docker-compose up -d
   ```

## 접속 정보

- **Jenkins**: http://localhost:8081
- **ngrok**: 실행 후 ngrok 이 발급하는 공개 URL 을 확인하여 GitHub Webhook 주소로 등록합니다.
