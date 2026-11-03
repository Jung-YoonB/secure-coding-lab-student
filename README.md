# secure-coding-lab (수강생 실습용)

금융권 보안 코딩 실습용 프로젝트 (Spring Boot 3.3 + MyBatis + Oracle, Java 17).
교안 [보안 코드 적용하기]를 보며 실습을 진행하고, 코드 내 `TODO` 주석이 표시된 **실습 문제**를 직접 완성해 본다.

> ⚠ `/vuln/**` 은 **일부러 취약하게 만든 코드**다. 로컬 실습 환경에서만 실행하고, 외부에 노출하거나 실제 서비스에 복사하지 않는다. 모든 데이터는 가상 데이터다.

## 직접 작성해 볼 TODO 실습 문제 안내

IDE에서 프로젝트 내 `TODO`를 검색하거나 아래 파일을 열어 주석에 적힌 **단계별 힌트(Step 1~4)**를 따라 코드를 작성해 보세요.

| 실습 문제 | 수정할 파일 위치 | 과제 설명 |
|---|---|---|
| **실습 문제 1** | 1) `src/main/resources/mapper/LoanMapper.xml`<br>2) `src/main/java/edu/secure/lab/mapper/LoanMapper.java` | 잔액 하한 조건(`balance >= #{minBalance}`)을 추가한 `findByCustomerMin` 바인딩 쿼리 및 메서드 작성 |
| **실습 문제 3** | `src/main/java/edu/secure/lab/security/MaskingUtil.java` | 카드번호(`1234-5678-9012-3456`)의 가운데 자리를 마스킹(`1234-****-****-3456`)하는 `maskCard` 메서드 작성 |
| **실습 문제 5** | `src/main/java/edu/secure/lab/service/LoanService.java` | `BigDecimal`과 `RoundingMode.DOWN`을 사용해 월 이자를 원 단위 절사로 계산하는 `monthlyInterestTruncated` 메서드 작성 |

> **자가 채점 방법**: 실습 문제 3번과 5번을 작성한 뒤 `src/test/java/edu/secure/lab/SecurityPracticeTest.java` 테스트를 실행(`mvn test` 또는 IDE에서 Run Test)하면 정답 여부를 즉시 확인할 수 있습니다!

---

## 구성

| 경로 | 설명 |
|---|---|
| `sql/01_schema.sql` | Oracle 테이블 생성 + 가상 데이터 |
| `web/VulnController`, `VulnPageController` | 취약 버전 (`/vuln/**`) |
| `web/SafeController`, `SafePageController` | 수정된 버전 (`/safe/**`) |
| `mapper/LoanMapper` + `resources/mapper/LoanMapper.xml` | MyBatis `${}` / `#{}` 비교 및 실습 문제 1 |
| `security/` | 마스킹(실습 문제 3), PBKDF2 해시, AES-GCM |
| `service/LoanService` | 정렬 허용 목록, 경쟁 조건, 금액 계산(실습 문제 5) |
| `web/ConcurrencyDemoController` | 경쟁 조건 시연 (`/lab/concurrency/{vuln\|safe}`) |
| `attack/csrf-attack.html` | CSRF 시연용 공격 페이지 |

## 실행 준비

1. **Oracle 21c XE 준비**: 관리자(`SYSTEM` 등)로 접속해 공통 사용자 계정(`C##SECURE`)을 만들고, 그 계정으로 `sql/01_schema.sql`을 스크립트로 실행한다 (SQL Developer: F5).

   ```sql
   CREATE USER C##SECURE IDENTIFIED BY SECURE DEFAULT TABLESPACE USERS QUOTA UNLIMITED ON USERS;
   GRANT CONNECT, RESOURCE TO C##SECURE;
   ```

2. **AES-128 키 생성 및 접속 정보 설정**
   - `src/test/java/edu/secure/lab/SecurityPracticeTest.java` 의 `generateAesKey()` 테스트를 실행하면 콘솔에 16바이트 Base64 난수 키가 출력된다.
   - 출력된 키를 환경 변수 `LAB_AES_KEY` (또는 로컬 실습 시 `application.properties`의 `lab.aes-key`)에 설정한다.

   ```powershell
   $env:LAB_DB_URL      = "jdbc:oracle:thin:@//localhost:1521/XE"
   $env:LAB_DB_USER     = "C##SECURE"
   $env:LAB_DB_PASSWORD = "SECURE"
   $env:LAB_AES_KEY     = "<generateAesKey() 테스트 실행 후 출력된 Base64 키>"
   ```

3. **실행** (Java 17, Maven 3.9+ 필요)

   ```powershell
   mvn spring-boot:run
   ```

   IntelliJ/STS 에서는 `LabApplication` 을 실행하면 된다.

## 실습 순서와 호출 예

로그인은 브라우저에서 `http://localhost:8080/login-page`, 또는:

```powershell
curl.exe -c cookie.txt -X POST "http://localhost:8080/login?customerId=C001"
```

| 장 | 취약 | 안전 |
|---|---|---|
| SQL 삽입 | `/vuln/sqli/loans?customerId=' OR '1'='1` | `/safe/sqli/loans?customerId=' OR '1'='1` |
| 정렬 삽입 / 오류 노출 | `/vuln/sqli/loans/sorted?customerId=C001&sortBy=nosuch` | `/safe/sqli/loans/sorted?customerId=C001&sortBy=nosuch` |
| IDOR (C001 로그인 후) | `/vuln/loans/1002` | `/safe/loans/1002` → 404 |
| 민감정보 | `/vuln/customers/C001` | `/safe/customers/C001` |
| 해시 | `POST /vuln/password/hash` | `POST /safe/password/hash` |
| 금액 계산 | `/vuln/interest?principal=1000000&rate=0.035` | `/safe/interest?principal=1000000&rate=0.035` |
| 경쟁 조건 | `/lab/concurrency/vuln` | `/lab/concurrency/safe` |
| XSS | `/vuln/search?q=<script>alert(1)</script>` | `/safe/search?q=<script>alert(1)</script>` |
| CSRF | `/vuln/repay-form` | `/safe/repay-form` |

### CSRF 시연

1. `/login-page` 에서 C001 로그인
2. 다른 포트에서 공격 페이지 열기

   ```powershell
   cd attack
   python -m http.server 9000
   ```

   브라우저에서 `http://localhost:9000/csrf-attack.html` 접속 → 대출 1001 잔액이 줄어든다.
3. 안전 버전은 토큰 검증 때문에 같은 방식으로는 실패한다.

## 정적 분석 (SpotBugs + Find Security Bugs)

```powershell
mvn -Psast compile spotbugs:spotbugs
mvn -Psast spotbugs:gui
```

리포트는 `target/spotbugsXml.xml` 에 생성된다.
