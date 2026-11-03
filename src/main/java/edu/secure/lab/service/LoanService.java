package edu.secure.lab.service;

import edu.secure.lab.domain.Loan;
import edu.secure.lab.mapper.LoanMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Service
public class LoanService {

    /** ORDER BY 허용 목록 */
    private static final Set<String> ALLOWED_SORT = Set.of("loan_id", "balance");

    private final LoanMapper loanMapper;

    public LoanService(LoanMapper loanMapper) {
        this.loanMapper = loanMapper;
    }

    // ---- 정렬: 허용 목록 검증 ------------------------------------------------

    /** [취약] 검증 없이 ${sortBy} 로 전달 */
    public List<Loan> sortedVuln(String customerId, String sortBy) {
        return loanMapper.findByCustomerSorted(customerId, sortBy);
    }

    /** [안전] 허용 목록에 있는 값만 통과 */
    public List<Loan> sortedSafe(String customerId, String sortBy) {
        if (!ALLOWED_SORT.contains(sortBy)) {
            throw new IllegalArgumentException("허용되지 않는 정렬 기준");
        }
        return loanMapper.findByCustomerSorted(customerId, sortBy);
    }

    // ---- 상환: 경쟁 조건 ------------------------------------------------------

    /** [취약] 조회 → 판단 → 갱신이 분리되어 동시 요청 시 잔액이 틀어진다 (Lost Update) */
    public boolean repayVuln(long loanId, long amount, long delayMillis) {
        Long balance = loanMapper.selectBalance(loanId);
        if (balance == null || balance < amount) {
            return false;
        }
        sleep(delayMillis);                       // 다른 요청이 끼어들 틈 (실습용)
        loanMapper.updateBalance(loanId, balance - amount);
        return true;
    }

    /** [안전] 소유자, 잔액 확인과 차감을 하나의 UPDATE 로 처리 */
    @Transactional
    public boolean repaySafe(long loanId, String customerId, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
        }
        return loanMapper.repayConditional(loanId, customerId, amount) == 1;
    }

    // ---- 금액 계산 --------------------------------------------------------------

    /** [취약] double 사용 + 절사 처리 불명확 */
    public long monthlyInterestVuln(long principal, double annualRate) {
        return (long) (principal * annualRate / 12);
    }

    /** [안전] BigDecimal + 반올림 규칙 명시 + 입력 검증 */
    public long monthlyInterestSafe(long principal, String annualRate) {
        BigDecimal rate = new BigDecimal(annualRate);
        if (principal <= 0) {
            throw new IllegalArgumentException("원금은 0보다 커야 합니다.");
        }
        if (rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("금리는 0 이상 1 이하여야 합니다.");
        }
        return BigDecimal.valueOf(principal)
                .multiply(rate)
                .divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    /**
     * [실습 문제 5] BigDecimal을 사용하여 월 이자를 '원 단위 절사(소수점 이하 버림)'로 계산하기
     * - 계산 공식: 원금(principal) * 연이율(annualRate) / 12개월
     * - 예시: 원금 100,000원, 연이율 "0.0275" (2.75%) 일 때
     *   100,000 * 0.0275 / 12 = 229.1666...원 → 원 단위 절사 시 229원이 나와야 합니다.
     */
    public long monthlyInterestTruncated(long principal, String annualRate) {
        // =====================================================================
        // TODO: 바로 위의 monthlyInterestSafe 메서드를 참고하여 작성해 보세요!
        //
        // 단계별 힌트:
        // Step 1. 원금(principal)을 BigDecimal.valueOf(principal) 로 변환합니다.
        // Step 2. 연이율 문자열(annualRate)은 오차가 없도록 new BigDecimal(annualRate) 로 생성한 뒤
        //         .multiply(...) 로 원금과 곱합니다.
        // Step 3. 12개월로 나누기 위해 .divide(BigDecimal.valueOf(12), 0, 반올림모드) 를 호출합니다.
        //         - 두 번째 인자 0 은 '소수점 0번째 자리(원 단위)까지 남긴다'는 뜻입니다.
        //         - 세 번째 인자에는 반올림(RoundingMode.HALF_UP) 대신
        //           절사(버림)를 뜻하는 RoundingMode.DOWN 을 넣으세요!
        // Step 4. 마지막에 .longValueExact() 를 붙여 long 타입으로 반환하세요.
        // =====================================================================

        return 0L; // TODO: 이 줄을 지우고 위 Step 1~4를 연결한 계산식을 반환하세요.
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
