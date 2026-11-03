package edu.secure.lab.security;

public final class MaskingUtil {

    private MaskingUtil() {
    }

    /** 900101-1234567 → 900101-1****** */
    public static String maskRrn(String rrn) {
        if (rrn == null || rrn.length() < 8 || rrn.indexOf('-') != 6) {
            return "******-*******";
        }
        return rrn.substring(0, 8) + "*".repeat(rrn.length() - 8);
    }

    /** 010-1111-2222 → 010-****-2222 */
    public static String maskPhone(String phone) {
        if (phone == null) {
            return null;
        }
        String[] p = phone.split("-");
        if (p.length != 3) {
            return "***";
        }
        return p[0] + "-" + "*".repeat(p[1].length()) + "-" + p[2];
    }

    /**
     * [실습 문제 3] 카드번호 마스킹 메서드 완성하기
     * - 입력 예시: "1234-5678-9012-3456"
     * - 출력 예시: "1234-****-****-3456" (앞 4자리와 뒤 4자리만 남기고 가운데는 * 처리)
     */
    public static String maskCard(String card) {
        // =====================================================================
        // TODO: 바로 위의 maskPhone(String phone) 메서드를 참고해서 작성해 보세요!
        //
        // 단계별 힌트:
        // Step 1. 만약 입력받은 card가 null이면 "****-****-****-****" 를 반환하세요.
        // Step 2. card.split("-") 를 사용해 하이픈("-") 기준으로 문자열을 쪼개어
        //         String[] 배열(예: String[] p)에 담으세요.
        //         ("1234-5678-9012-3456"을 쪼개면 p[0]="1234", p[1]="5678", p[2]="9012", p[3]="3456" 총 4칸이 됩니다)
        // Step 3. 만약 배열의 길이(p.length)가 4가 아니라면(정상 카드번호 형식이 아니면)
        //         "****-****-****-****" 를 반환하세요.
        // Step 4. 첫 번째 조각(p[0])과 네 번째 조각(p[3]) 사이에 "-****-****-"를 이어 붙여 반환하세요.
        // =====================================================================
    	if (card == null) {
    		return "****-****-****-****";
    	}
    	
    	String[] c = card.split("-");
    	
    	if (c.length !=4) {
    		return "****-****-****-****";
    	}
    	
    	return c[0] + "-"+ "*".repeat(c[1].length()) + "-" + "*".repeat(c[2].length()) + "-" + c[3];
    }
}
