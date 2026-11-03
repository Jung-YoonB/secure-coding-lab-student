package edu.secure.lab;

import edu.secure.lab.security.MaskingUtil;
import edu.secure.lab.security.PasswordHasher;
import edu.secure.lab.service.LoanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 수강생 자가 점검 및 AES 키 생성용 테스트 코드.
 * 1) generateAesKey() 를 실행하면 콘솔에 AES-128 키(Base64)가 출력됩니다.
 * 2) 실습 문제 3번(MaskingUtil.maskCard)과 실습 문제 5번(LoanService.monthlyInterestTruncated)을
 *    작성한 뒤 테스트를 실행해 초록불(통과)이 뜨는지 확인하세요!
 */
class SecurityPracticeTest {

    @Test
    @DisplayName("[사전 준비] AES-128 암호화 키(Base64) 생성기 — 실행 후 콘솔 출력값을 복사해 쓰세요")
    void generateAesKey() {
        byte[] key = new byte[16]; // 16바이트 = 128비트
        new SecureRandom().nextBytes(key);
        String base64Key = Base64.getEncoder().encodeToString(key);

        System.out.println("============================================================");
        System.out.println("[생성된 AES-128 키 (Base64)] : " + base64Key);
        System.out.println("-> application.properties 의 lab.aes-key 또는 환경 변수 LAB_AES_KEY 에 넣어주세요.");
        System.out.println("============================================================");

        assertEquals(16, Base64.getDecoder().decode(base64Key).length);
    }

    @Test
    @DisplayName("[실습 문제 3 확인] 카드번호 마스킹 테스트")
    void testMaskCard() {
        // 기본 마스킹 동작 확인
        assertEquals("900101-1******", MaskingUtil.maskRrn("900101-1234567"));
        assertEquals("010-****-2222", MaskingUtil.maskPhone("010-1111-2222"));

        // TODO: MaskingUtil.maskCard() 구현을 완료하면 아래 테스트가 통과합니다!
        assertEquals("1234-****-****-3456", MaskingUtil.maskCard("1234-5678-9012-3456"));
        assertEquals("****-****-****-****", MaskingUtil.maskCard(null));
        assertEquals("****-****-****-****", MaskingUtil.maskCard("invalid-card"));
    }

    @Test
    @DisplayName("[실습 문제 4 확인] 솔트 없는 MD5 vs 무작위 솔트 PBKDF2 비교")
    void testPasswordHasher() {
        // 1. 취약 MD5: 같은 비밀번호를 넣으면 항상 똑같은 해시값이 나옴
        String md5First = PasswordHasher.md5("pass1234");
        String md5Second = PasswordHasher.md5("pass1234");
        assertEquals(md5First, md5Second);

        // 2. 안전 PBKDF2 + 무작위 솔트: 같은 비밀번호라도 호출할 때마다 결과 문자열이 다름
        String safeFirst = PasswordHasher.hash("pass1234");
        String safeSecond = PasswordHasher.hash("pass1234");
        assertNotEquals(safeFirst, safeSecond);

        // 검증 시에는 저장된 솔트를 꺼내어 비교하므로 모두 true가 나옴
        assertTrue(PasswordHasher.verify("pass1234", safeFirst));
        assertTrue(PasswordHasher.verify("pass1234", safeSecond));
        assertFalse(PasswordHasher.verify("wrongpass", safeFirst));
    }

    @Test
    @DisplayName("[실습 문제 5 확인] BigDecimal 원 단위 절사(RoundingMode.DOWN) 월 이자 계산")
    void testMonthlyInterestTruncated() {
        LoanService service = new LoanService(null);

        // 반올림(HALF_UP) 기본 제공 메서드 확인
        assertEquals(2917L, service.monthlyInterestSafe(1_000_000L, "0.035"));

        // TODO: LoanService.monthlyInterestTruncated() 구현을 완료하면 아래 테스트가 통과합니다!
        // 100,000 * 0.0275 / 12 = 229.1666... → 절사 시 229원
        assertEquals(229L, service.monthlyInterestTruncated(100_000L, "0.0275"));
    }
}
