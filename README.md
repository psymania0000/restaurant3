# Restaurant Management System

레스토랑 관리 시스템은 레스토랑의 예약, 메뉴, 리뷰 등을 관리할 수 있는 웹 애플리케이션입니다.

## 주요 기능

- 레스토랑 관리 (추가, 수정, 삭제)
- 메뉴 관리 (추가, 수정, 삭제)
- 예약 관리
- 리뷰 관리
- 사용자 관리

## 기술 스택

- Backend: Spring Boot 3.x
- Frontend: Thymeleaf, Bootstrap 5
- Database: MariaDB
- Security: Spring Security
- Build Tool: Maven

## 시작하기

### 필수 조건

- Java 17 이상
- Maven
- MariaDB

### 설치 및 실행

1. 저장소 클론
```bash
git clone https://github.com/psymania0000/restaurant2.git
```

2. 데이터베이스 설정
- MariaDB에 `restaurant_db` 데이터베이스 생성
- `src/main/resources/application.properties` 파일에서 데이터베이스 연결 정보 수정

3. 애플리케이션 실행
```bash
mvn spring-boot:run
```

4. 웹 브라우저에서 접속
```
http://localhost:8080
```

## 기본 계정

- 관리자
  - ID: admin
  - 비밀번호: admin

## 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다. 