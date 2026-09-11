# QR Generator Frontend

URL을 입력해 QR 코드를 생성하고 PNG 파일로 내려받는 React/Vite 프런트엔드입니다.

## 요구 사항

- Node.js 및 npm
- 실행 중인 `url-to-qr` 백엔드

## 프로젝트 위치

프런트엔드 애플리케이션은 저장소의 `frontend` 디렉터리에 있습니다.

## 환경변수

`frontend/.env` 파일을 생성합니다.

```dotenv
VITE_QR_API_BASE_URL=http://localhost:8080
```

Nginx가 `/api` 요청을 백엔드로 프록시하는 배포 환경에서는 다음 값을 사용합니다.

```dotenv
VITE_QR_API_BASE_URL=/api
```

API 주소 적용 우선순위는 다음과 같습니다.

1. 실행 프로세스의 `VITE_QR_API_BASE_URL`
2. `frontend/.env`의 `VITE_QR_API_BASE_URL`
3. 기본값 `http://localhost:8080`

서버 시작 전에 `scripts/generate-runtime-config.cjs`가 값을 읽어 `runtime-config.js`를 생성하므로 빌드 이후에도 API 주소를 변경할 수 있습니다.

## 개발 서버 실행

```bash
cd frontend
npm ci
npm run dev
```

기본 접속 주소는 `http://127.0.0.1:5173`입니다. `predev` 스크립트가 먼저 실행되어 `public/runtime-config.js`를 생성합니다.

> 백엔드에는 현재 CORS 허용 설정이 없습니다. 프런트와 백엔드를 서로 다른 origin으로 실행하면 브라우저 요청이 차단될 수 있으므로, 통합 환경에서는 Nginx 같은 동일 origin 프록시가 `/api`를 백엔드로 전달하도록 구성하는 방식을 권장합니다.

## 프로덕션 빌드

```bash
cd frontend
npm ci
npm run build
```

빌드 결과는 `frontend/dist/`에 생성됩니다.

빌드 결과 미리보기:

```bash
npm run preview
```

`prepreview` 스크립트가 현재 환경변수 또는 `.env`를 읽어 `dist/runtime-config.js`를 다시 생성합니다.

## 정적 웹서버 및 Docker 배포

Nginx 등으로 `dist`를 직접 서빙할 때는 웹서버 시작 전에 런타임 설정을 생성합니다.

```bash
cd frontend
npm run runtime:config -- --output dist/runtime-config.js
```

미리 빌드한 결과만 Docker/Nginx 서버에 배포할 때 필요한 프런트 파일은 다음과 같습니다.

```text
dist/
runtime-config.template.js
docker-entrypoint.sh
```

`docker-entrypoint.sh`는 컨테이너 시작 시 `runtime-config.template.js`에 `VITE_QR_API_BASE_URL`을 적용해 `/usr/share/nginx/html/runtime-config.js`를 생성합니다.

## 주요 명령어

1. `npm run dev` = 개발 서버 실행
2. `npm run build` = 프로덕션 정적 파일 빌드
3. `npm run preview` = 빌드 결과 미리보기
4. `npm run runtime:config` = `public/runtime-config.js` 생성
5. `npm run runtime:config -- --output dist/runtime-config.js` = 배포용 런타임 설정 생성

## API 요청

```http
POST {VITE_QR_API_BASE_URL}/create-qr
Content-Type: text/plain

https://example.com
```

성공하면 PNG 이미지를 받아 화면에 표시하고 다운로드할 수 있습니다.
