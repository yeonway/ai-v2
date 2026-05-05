# Phone AI Bridge Android

Phone AI Bridge Android는 Raspberry Pi 5의 Minecraft Paper/FastAPI 환경에서 Android 폰으로 로컬 네트워크 HTTP 요청을 보내면, 폰이 AI/RAG API 서버처럼 응답하는 MVP Android 앱입니다.

## 현재 MVP 범위

- Jetpack Compose 화면: Home, Settings, Memory, Knowledge, Logs
- Foreground Service 기반 로컬 HTTP 서버 유지
- `GET /health` 공개 상태 확인
- `POST /api/ask` Mock AI 응답 생성
- `X-API-Token` 기반 `/api/*` 인증
- Room DB 기반 플레이어 기억, 지식 문서, RAG chunk, 요청 로그 저장
- Keyword RAG 검색
- LiteRT-LM/Gemma 실제 로딩은 `LiteRtLmAiEngine` TODO stub으로 분리

## 실행 방법

```bash
gradle :app:assembleDebug
```

Android Studio에서 프로젝트를 열고 앱을 설치한 뒤 Home 화면에서 **Service 시작**을 누릅니다. Android 13 이상에서는 알림 권한을 허용해야 Foreground Service 알림이 정상 표시됩니다.

## API Token 설정 방법

Settings 화면에서 앱 최초 실행 시 자동 생성된 API Token을 확인합니다. 필요하면 **토큰 재생성**을 누르고 **설정 저장**을 누릅니다.

- 기본 포트: `8765`
- 허용 Raspberry Pi IP가 비어 있으면 토큰만 검사합니다.
- 허용 IP를 입력하면 해당 IP와 API Token이 모두 맞아야 `/api/*` 요청이 허용됩니다.

## `/health` 테스트

```bash
curl http://PHONE_IP:8765/health
```

## `/api/ask` 예시

```bash
curl -X POST http://PHONE_IP:8765/api/ask \
  -H "Content-Type: application/json" \
  -H "X-API-Token: YOUR_TOKEN" \
  -d '{
    "player_uuid": "test-uuid",
    "player_name": "Steve",
    "message": "철팜 어디야?",
    "server_context": "TPS 20.0, 접속자 2명",
    "coordinate_context": "철팜: overworld x=100 y=60 z=100",
    "spark_context": "",
    "max_tokens": 160
  }'
```

## RAG 문서 등록 예시

```bash
curl -X POST http://PHONE_IP:8765/api/rag/ingest \
  -H "Content-Type: application/json" \
  -H "X-API-Token: YOUR_TOKEN" \
  -d '{
    "title": "철팜 기본 조건",
    "content": "철팜은 주민, 침대, 작업대, 골렘 스폰 공간 조건이 중요하다.",
    "source_type": "manual",
    "tags": "iron_farm,villager,golem"
  }'
```

## RAG 검색 예시

```bash
curl -X POST http://PHONE_IP:8765/api/rag/search \
  -H "Content-Type: application/json" \
  -H "X-API-Token: YOUR_TOKEN" \
  -d '{"query":"철팜 골렘 안 나옴","limit":5}'
```

## 2차 작업 TODO

- LiteRT-LM 실제 의존성 추가
- Gemma 4 E2B 4bit 모델 실제 로드
- 스트리밍 응답
- 벡터 임베딩 검색
- Raspberry Pi FastAPI 실제 연동 테스트
- Paper 플러그인 채팅 연동
- 성능/발열 최적화

## 보안/역할 제한

Android 앱은 외부 인터넷 공개 서버, 포트포워딩, ngrok/public URL 기능을 제공하지 않습니다. Minecraft 명령어 실행, 월드 블록 수정, 백업/삭제/복구 자동화도 수행하지 않습니다. 좌표 DB와 NAS 백업은 Raspberry Pi/Synology 측 책임이며, Android 앱은 전달받은 context를 참고해 답변만 생성합니다.
