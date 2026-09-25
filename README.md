# URL to QR Backend

URL을 QR 코드 이미지로 변환하고 생성 이력을 MySQL에 저장하는 Spring Boot 애플리케이션입니다.

## 프로젝트 위치

백엔드 애플리케이션은 저장소의 `backend` 디렉터리에 있습니다. 

## 요구 사항

- JDK 17 이상
- MySQL 8.x
- QR 이미지 저장 디렉터리

## 데이터베이스 준비

애플리케이션 실행 전에 MySQL이 실행 중이어야 합니다. 
MySQL에 관리자 계정으로 접속한 뒤 데이터베이스와 애플리케이션 사용자를 생성합니다.

```sql
CREATE DATABASE url2qr
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
	
CREATE USER 'qruser'@'%'
    IDENTIFIED BY 'change-me';

GRANT ALL PRIVILEGES ON url2qr.*
    TO 'qruser'@'%';

FLUSH PRIVILEGES;
```

JPA의 `spring.jpa.hibernate.ddl-auto=update` 설정에 따라 테이블은 애플리케이션 시작 시 자동으로 생성 또는 갱신됩니다. 
데이터베이스와 접속 사용자는 미리 준비해야 합니다.

## 환경변수

1. `SERVER_PORT` = HTTP 서버 포트
2. `DB_URL` = MySQL JDBC URL 
3. `DB_USERNAME` = MySQL 사용자 
4. `DB_PASSWORD` = MySQL 비밀번호
5. `QR_UPLOAD_PATH` = QR 이미지 저장 디렉터리 

Spring Boot는 `.env` 파일을 자동으로 읽지 않습니다. 
직접 실행할 때는 셸 환경변수로 설정하고, Docker Compose에서는 `env_file`로 컨테이너에 전달합니다.
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`는 위에서 생성한 데이터베이스 및 사용자 정보와 일치해야 합니다.

### Windows PowerShell

```powershell
cd backend

$env:SERVER_PORT = "8080"
$env:DB_URL = "jdbc:mysql://localhost:3306/url2qr"
$env:DB_USERNAME = "qruser"
$env:DB_PASSWORD = "change-me"
$env:QR_UPLOAD_PATH = "./output"

New-Item -ItemType Directory -Force ./output
```

### Linux/macOS

```bash
cd backend

export SERVER_PORT=8080
export DB_URL=jdbc:mysql://localhost:3306/url2qr
export DB_USERNAME=qruser
export DB_PASSWORD=change-me
export QR_UPLOAD_PATH=./output

mkdir -p "$QR_UPLOAD_PATH"
```

환경변수는 현재 터미널 프로세스에 설정됩니다.
값을 설정한 터미널에서 이어서 Spring Boot를 실행해야 하며, 새 터미널을 열면 다시 설정해야 합니다.

## 애플리케이션 실행

Windows:

```powershell
./mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

환경변수 예시 기준 접속 주소는 `http://localhost:8080`입니다.

## 실행 JAR 빌드

Windows:

```powershell
cd backend
./mvnw.cmd clean package -DskipTests
```

Linux/macOS:

```bash
cd backend
./mvnw clean package -DskipTests
```

실행 가능한 JAR은 다음 위치에 생성됩니다.

```text
backend/target/url-to-qr-0.0.1-SNAPSHOT.jar
```

실행:

```bash
java -jar target/url-to-qr-0.0.1-SNAPSHOT.jar
```

JAR 실행 시에도 필수 환경변수가 설정되어 있어야 합니다.

## API

### QR 코드 생성

```http
POST /create-qr
Content-Type: text/plain
Accept: image/png

https://example.com
```

```bash
curl -X POST http://localhost:8080/create-qr \
  -H "Content-Type: text/plain" \
  -H "Accept: image/png" \
  --data "https://example.com" \
  --output qr-code.png
```

성공하면 `200 OK`, `image/png` 응답과 생성 파일명이 포함된 `Content-Disposition` 헤더를 반환합니다. 
유효하지 않은 URL은 `400 Bad Request`와 `application/problem+json` 응답을 반환합니다.

## 상태 확인

```text
GET /actuator/health/app
GET /actuator/health/dependencies
```

- `app`: 애플리케이션 ping 상태
- `dependencies`: 데이터베이스 연결 상태
