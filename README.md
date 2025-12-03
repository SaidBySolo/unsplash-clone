# Unsplash Clone

과제용

## 프로덕션 실행

.env 파일을 설정한 후, 다음 명령어로 도커 컴포즈를 사용하여 프로덕션 환경에서 애플리케이션을 빌드하고 실행합니다.

```sh
docker compose -f compose.prod.yaml --env-file .env up -d --build
```