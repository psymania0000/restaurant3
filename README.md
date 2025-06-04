# Restaurant Reservation System

레스토랑 예약 시스템은 사용자가 레스토랑을 검색하고, 메뉴를 확인하며, 예약을 할 수 있는 웹 애플리케이션입니다.

## 주요 기능

- 사용자 관리 (회원가입, 로그인, 프로필 관리)
- 레스토랑 검색 및 상세 정보 조회
- 메뉴 관리 및 조회
- 예약 시스템
- 리뷰 시스템
- 관리자 기능 (레스토랑 관리, 사용자 관리)

## 기술 스택

- Backend: Spring Boot
- Frontend: Thymeleaf, Bootstrap 5
- Database: MariaDB
- Security: Spring Security, JWT
- Build Tool: Maven

## 시작하기

### 필수 조건

- Java 17 이상
- Maven
- MariaDB

### 설치 및 실행

1. 저장소 클론
```bash
git clone [repository-url]
cd restaurant
```

2. 데이터베이스 설정
- MariaDB 설치 및 실행
- 데이터베이스 생성: `restaurant`
- 사용자 생성: `webuser` / 비밀번호: `webuser`

3. 애플리케이션 실행
```bash
mvn spring-boot:run
```

4. 웹 브라우저에서 접속
```
http://localhost:8080
```

## 프로젝트 구조

```
src/main/java/com/restaurant/
├── config/         # 설정 파일
├── controller/     # 컨트롤러
├── dto/           # 데이터 전송 객체
├── entity/        # 엔티티
├── repository/    # 리포지토리
├── service/       # 서비스
└── security/      # 보안 관련
```

## API 문서

- 메인 페이지: `/`
- 로그인: `/login`
- 회원가입: `/signup`
- 레스토랑 목록: `/restaurants`
- 예약: `/restaurants/{id}/reserve`

## 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다. 