package edu.secure.lab.web;

import edu.secure.lab.domain.Customer;
import edu.secure.lab.domain.Loan;
import edu.secure.lab.mapper.CustomerMapper;
import edu.secure.lab.mapper.LoanMapper;
import edu.secure.lab.security.PasswordHasher;
import edu.secure.lab.service.LoanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ⚠ 교육용 취약 코드. 이 컨트롤러의 모든 엔드포인트는 일부러 취약하게 작성되었다.
 * 실제 서비스에 복사하지 않는다.
 */
@RestController
@RequestMapping("/vuln")
public class VulnController {

    private static final Logger log = LoggerFactory.getLogger(VulnController.class);

    private final LoanMapper loanMapper;
    private final CustomerMapper customerMapper;
    private final LoanService loanService;

    public VulnController(LoanMapper loanMapper, CustomerMapper customerMapper, LoanService loanService) {
        this.loanMapper = loanMapper;
        this.customerMapper = customerMapper;
        this.loanService = loanService;
    }

    // 1. SQL 삽입 -------------------------------------------------------------
    @GetMapping("/sqli/loans")
    public List<Loan> loans(@RequestParam String customerId) {
        return loanMapper.findByCustomerVuln(customerId);            // ${} 치환
    }

    @GetMapping("/sqli/loans/sorted")
    public List<Loan> sorted(@RequestParam String customerId, @RequestParam String sortBy) {
        return loanService.sortedVuln(customerId, sortBy);           // 허용 목록 검증 없음
    }

    // 2. IDOR -----------------------------------------------------------------
    @GetMapping("/loans/{loanId}")
    public Loan loan(@PathVariable long loanId) {
        return loanMapper.findById(loanId);                          // 소유자 확인 없음
    }

    // 3. 민감정보 노출 ---------------------------------------------------------
    @GetMapping("/customers/{customerId}")
    public Customer customer(@PathVariable String customerId) {
        Customer c = customerMapper.findById(customerId);
        log.info("고객 조회: {}", c);                                // 로그에 주민번호 등 그대로 기록
        return c;                                                    // 원본 그대로 응답
    }

    /** 오류 상세(SQL, 테이블명 등)를 그대로 사용자에게 노출 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
    }

    // 4. 해시 -----------------------------------------------------------------
    @PostMapping("/password/hash")
    public Map<String, String> hash(@RequestParam String password) {
        return Map.of("md5", PasswordHasher.md5(password));          // 솔트 없음, 빠른 해시
    }

    // 5. 금액·동시성 ----------------------------------------------------------
    @GetMapping("/interest")
    public Map<String, Object> interest(@RequestParam long principal, @RequestParam double rate) {
        return Map.of("monthlyInterest", loanService.monthlyInterestVuln(principal, rate));
    }

    @PostMapping("/loans/{loanId}/repay")
    public Map<String, Object> repay(@PathVariable long loanId, @RequestParam long amount) {
        // 금액 검증 없음(음수 가능), 소유자 확인 없음
        return Map.of("success", loanService.repayVuln(loanId, amount, 0));
    }
}
