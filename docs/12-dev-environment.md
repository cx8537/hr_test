# 12. 개발 환경 (Windows 11, Docker 미사용)

로컬에서 네 개의 프로세스를 네이티브로 구동한다: **PostgreSQL, Spring Boot, Next.js, MinIO**. Docker는 사용하지 않는다.

---

## 1. 설치 대상 (네이티브)
| 구성요소 | 권장 | 비고 |
|---------|------|------|
| JDK | 21 LTS | Spring Boot 3.x |
| 빌드 도구 | Gradle | Maven 대체 가능 |
| Node.js | LTS | Next.js 빌드·구동 |
| PostgreSQL | Windows 인스톨러 | 개발 DB + 테스트 스키마 |
| MinIO | minio.exe(Windows 바이너리) | 단일 노드, Docker 없이 |

> 모든 자격증명·키는 환경변수/설정파일로 외부화하고 버전관리에 커밋하지 않는다(NFR-OPS-002).

## 2. 프로세스 구성 (로컬)
```
[PostgreSQL]  : 5432 (예)  — 업무 DB
[MinIO]       : 9000 API / 9001 콘솔 (예) — 파일 저장(minio.exe server)
[Spring Boot] : 8080 (예)  — REST API
[Next.js]     : 3000 (예)  — 프론트 dev 서버 (CSR)
```
- 프론트(3000) → 백엔드(8080) 호출 시 포트가 다르므로 **개발 CORS 허용** 설정 필요.
- MinIO는 백엔드(8080)만 접근. 프론트는 MinIO에 직접 접근하지 않는다(백엔드 경유).

## 3. 환경 변수(예시 키 목록)
- DB: `DB_URL`, `DB_USER`, `DB_PASSWORD`
- JWT: `JWT_SECRET`, `JWT_ACCESS_EXPIRES_IN`, `JWT_REFRESH_EXPIRES_IN`
- MinIO: `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET`
- 부트스트랩: `BOOTSTRAP_ADMIN_LOGIN_ID`, `BOOTSTRAP_ADMIN_INIT_PASSWORD`(최초 관리자, 최초 로그인 시 변경 강제)
- 지도(프론트): `NEXT_PUBLIC_MAP_API_KEY`

## 4. 빌드·실행 순서(개발)
1. PostgreSQL 기동, DB·테스트 스키마 생성.
2. MinIO 기동(`minio.exe server <data-dir>`), 버킷 생성.
3. Spring Boot 기동(Gradle) — 기동 시 **Flyway 마이그레이션 적용**(스키마 + 최초 시스템관리자 시드), DB·MinIO 연결 확인.
4. Next.js dev 서버 기동 — 백엔드 API 연동 확인.
- 최초 로그인은 시드된 관리자(`BOOTSTRAP_ADMIN_*`)로 하며, 즉시 비밀번호 변경이 요구된다.

## 5. 테스트 실행
- 백엔드 단위 테스트: 도메인 로직(DB 불필요) — 빠르게 자주.
- 통합 테스트: 로컬 PostgreSQL **테스트 스키마** 사용(운영 데이터와 분리). Docker 미사용이므로 Testcontainers 대신 로컬 인스턴스 활용.
- 프론트 테스트: 컴포넌트·로직 단위(폼 분기, 일수 표시, 서명 호환성 등).
- CI가 없다면 로컬에서 `테스트 → 빌드` 순으로 확인 후 커밋.

## 6. 프로젝트 구조(권장 개요)
```
/ (repo root)
  CLAUDE.md
  /docs                      ← 본 요구사항 문서들
  /backend                   ← Spring Boot
    /src/main/java/.../auth
                    .../org
                    .../signature
                    .../approval
                    .../location
                    .../asset
                    .../reservation
                    .../document
                    .../notification
    /src/test/java/...       ← 단위/통합 테스트(요구사항 ID 매핑)
  /frontend                  ← Next.js (App Router, CSR) — 단일 앱
    /app
      /(portal)              ← 일반 업무 + 범위 관리 화면(역할별 동적 노출)
      /(admin)               ← 시스템 관리 화면(역할 가드로 보호)
    /components, /lib
    /__tests__
```
- 백엔드 패키지 경계 = 모듈 경계(09 문서 §6). 모듈 간은 인터페이스/도메인 이벤트로 결합 최소화.

## 7. 주의 (자주 실수하는 부분)
- Docker로 MinIO/PostgreSQL을 띄우지 말 것(네이티브만).
- presigned URL로 파일을 프론트에 직접 노출하지 말 것(백엔드 경유).
- 개인키를 서버·DB에 저장하지 말 것(공개키만).
- 시간 의존 로직은 `Clock` 주입(테스트 결정론성). **타임존은 KST(Asia/Seoul) 고정**(서버 JVM·PostgreSQL·브라우저 일치).
- 스키마는 손으로 바꾸지 말고 **Flyway 마이그레이션**으로(운영 `ddl-auto=validate`).
- 비밀값을 코드/저장소에 커밋하지 말 것.
