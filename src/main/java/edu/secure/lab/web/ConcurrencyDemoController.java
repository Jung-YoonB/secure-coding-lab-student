package edu.secure.lab.web;

import edu.secure.lab.mapper.LoanMapper;
import edu.secure.lab.service.LoanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 경쟁 조건 시연. 대출 1003(소유자 C003, 잔액 500만 원)에 대해
 * 400만 원 상환을 동시에 2번 요청해 최종 잔액을 비교한다.
 *  - 정상 기대값: 1건 성공, 잔액 100만 원
 *  - 취약 버전: 2건 성공, 잔액 100만 원(두 번째 차감이 사라짐) 또는 비정상 결과
 */
@RestController
public class ConcurrencyDemoController {

    private static final long LOAN_ID = 1003;
    private static final String OWNER = "C003";
    private static final long INITIAL = 5_000_000;
    private static final long AMOUNT = 4_000_000;

    private final LoanService loanService;
    private final LoanMapper loanMapper;

    public ConcurrencyDemoController(LoanService loanService, LoanMapper loanMapper) {
        this.loanService = loanService;
        this.loanMapper = loanMapper;
    }

    @GetMapping("/lab/concurrency/{mode}")
    public Object run(@PathVariable String mode) {
        boolean vuln = "vuln".equals(mode);
        if (!vuln && !"safe".equals(mode)) {
            throw new IllegalArgumentException("mode 는 vuln 또는 safe");
        }
        loanMapper.updateBalance(LOAN_ID, INITIAL);                   // 초기화

        AtomicInteger success = new AtomicInteger();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                boolean ok = vuln
                        ? loanService.repayVuln(LOAN_ID, AMOUNT, 300)
                        : loanService.repaySafe(LOAN_ID, OWNER, AMOUNT);
                if (ok) {
                    success.incrementAndGet();
                }
            }));
        }
        futures.forEach(CompletableFuture::join);

        return java.util.Map.of(
                "mode", mode,
                "successCount", success.get(),
                "initialBalance", INITIAL,
                "finalBalance", loanMapper.selectBalance(LOAN_ID));
    }
}
