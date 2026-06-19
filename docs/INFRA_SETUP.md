# 로컬 개발 인프라 셋업 (Windows 11, Docker 미사용)

로컬에서 네 프로세스를 네이티브로 구동한다: **PostgreSQL, MinIO, Spring Boot, Next.js**.
상세 배경은 `12-dev-environment.md` 참고. 이 문서는 실제 셋업 절차다.

## 1. PostgreSQL (개발 DB + 테스트 스키마)
PostgreSQL 18이 Windows 서비스(`postgresql-x64-18`)로 설치·기동되어 있다고 가정한다(포트 5432).

```sql
-- superuser(postgres)로 1회 실행. 예) psql -U postgres -h localhost
CREATE ROLE hr WITH LOGIN PASSWORD '<설정할_비밀번호>';
CREATE DATABASE hr OWNER hr;

-- 통합 테스트용 별도 스키마(운영 데이터와 분리). 동일 DB 내 test 스키마 사용 예시:
\connect hr
CREATE SCHEMA IF NOT EXISTS test AUTHORIZATION hr;
```

- 스키마 객체는 손으로 만들지 않는다. **Flyway 마이그레이션**이 백엔드 기동 시 적용한다.
- 시각은 **KST(Asia/Seoul)** 로 통일(서버 JVM·DB·브라우저 일치).

## 2. MinIO (파일 저장, 네이티브 바이너리)
Docker를 쓰지 않고 `minio.exe` 를 직접 구동한다(API 9000 / 콘솔 9001).

```powershell
# 데이터 디렉토리를 정해 서버 기동
$env:MINIO_ROOT_USER = "<access-key>"
$env:MINIO_ROOT_PASSWORD = "<secret-key>"
minio.exe server "D:\minio-data" --console-address ":9001"
```

- 기동 후 콘솔(http://localhost:9001) 또는 `mc` 로 버킷 `hr` 을 생성한다.
- 프론트는 MinIO에 직접 접근하지 않는다. **모든 파일 접근은 백엔드(8080) 경유**, presigned URL 미사용.

## 3. 백엔드 (Spring Boot)
1. `backend/src/main/resources/application-local.yml.example` 을 복사해 `application-local.yml` 생성 후 실제 값 입력(이 파일은 커밋 금지).
2. `local` 프로파일로 기동:
   ```powershell
   cd backend
   .\gradlew.bat bootRun --args='--spring.profiles.active=local'
   ```
   - 기동 시 Flyway 마이그레이션이 적용되고(`ddl-auto=validate`), DB 연결을 검증한다.
3. 헬스 체크: `GET http://localhost:8080/api/health` → `{"status":"UP","db":"UP"}` 확인.

## 4. 프론트엔드 (Next.js)
1. `frontend/.env.local.example` 을 복사해 `.env.local` 생성(`NEXT_PUBLIC_API_BASE_URL=http://localhost:8080`).
2. 개발 서버 기동:
   ```powershell
   cd frontend
   npm install
   npm run dev   # http://localhost:3000
   ```
- 프론트(3000) → 백엔드(8080) 호출은 포트가 다르므로 백엔드의 **개발 CORS 허용**(`CORS_ALLOWED_ORIGINS`, 기본 `http://localhost:3000`)이 적용된다.

## 5. 기동 순서 요약
1. PostgreSQL (서비스, 5432)
2. MinIO (`minio.exe server`, 9000/9001) + 버킷 `hr`
3. Spring Boot (8080, `local` 프로파일) — Flyway 적용 + 헬스 체크
4. Next.js (3000)

## 6. 환경변수 / 비밀 관리
- 비밀값(DB 비밀번호, JWT 시크릿, MinIO 키, 부트스트랩 관리자 초기 비밀번호)은 `application-local.yml`(백엔드)·`.env.local`(프론트)에만 두고 **버전관리에 커밋하지 않는다**(NFR-OPS-002).
- 예시 키 목록: `12-dev-environment.md` §3.
