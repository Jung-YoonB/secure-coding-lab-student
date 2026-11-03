package edu.secure.lab.web;

import edu.secure.lab.domain.Customer;
import edu.secure.lab.domain.CustomerView;
import edu.secure.lab.domain.Loan;
import edu.secure.lab.mapper.CustomerMapper;
import edu.secure.lab.mapper.LoanMapper;
import edu.secure.lab.security.AesGcmCrypto;
import edu.secure.lab.security.MaskingUtil;
import edu.secure.lab.security.PasswordHasher;
import edu.secure.lab.service.LoanService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/** 수정된 안전 버전. VulnController 와 같은 기능을 안전하게 구현한다. */
@RestController
@RequestMapping("/safe")
public class SafeController {

    private static final Logger log = LoggerFactory.getLogger(SafeController.class);

    private final LoanMapper loanMapper;
    private final CustomerMapper customerMapper;
    private final LoanService loanService;
    private final AesGcmCrypto crypto;

    public SafeController(LoanMapper loanMapper, CustomerMapper customerMapper,
                          LoanService loanService, AesGcmCrypto crypto) {
        this.loanMapper = loanMapper;
        this.customerMapper = customerMapper;
        this.loanService = loanService;
        this.crypto = crypto;
    }

    // 1. SQL 삽입 방어 ---------------------------------------------------------
    // (이 엔드포인트는 SQL 삽입 방어만 보여 주기 위해 customerId 를 파라미터로 받는다. 소유권 문제는 2장에서 해결)
    @GetMapping("/sqli/loans")
    public List<Loan> loans(@RequestParam String customerId) {
        return loanMapper.findByCustomerSafe(customerId);            // #{} 바인딩
    }

    @GetMapping("/sqli/loans/sorted")
    public List<Loan> sorted(@RequestParam String customerId, @RequestParam String sortBy) {
        return loanService.sortedSafe(customerId, sortBy);           // 허용 목록 검증
    }

    // 2. IDOR 방어 ------------------------------------------------------------
    @GetMapping("/loans/{loanId}")
    public Loan loan(@PathVariable long loanId, HttpSession session) {
        String me = LoginController.requireCustomer(session);
        Loan loan = loanMapper.findByIdAndCustomer(loanId, me);      // 소유자 조건 포함
        if (loan == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND); // 타인 것/없는 것 구분하지 않음
        }
        return loan;
    }

    // 3. 민감정보 보호 ---------------------------------------------------------
    @GetMapping("/customers/{customerId}")
    public CustomerView customer(@PathVariable String customerId, HttpSession session) {
        String me = LoginController.requireCustomer(session);
        if (!me.equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Customer c = customerMapper.findById(customerId);
        log.info("고객 조회: customerId={}", customerId);            // 식별자만 기록
        return new CustomerView(c.getCustomerId(), c.getCustomerName(),
                MaskingUtil.maskRrn(c.getRrn()), MaskingUtil.maskPhone(c.getPhone()));
    }

    // 4. 해시, 암호화 ------------------------------------------------------------
    @PostMapping("/password/hash")
    public Map<String, String> hash(@RequestParam String password) {
        return Map.of("stored", PasswordHasher.hash(password));      // 솔트 + PBKDF2
    }

    @PostMapping("/password/verify")
    public Map<String, Boolean> verify(@RequestParam String password, @RequestParam String stored) {
        return Map.of("match", PasswordHasher.verify(password, stored));
    }

    @PostMapping("/crypto/encrypt")
    public Map<String, String> encrypt(@RequestParam String text) {
        return Map.of("token", crypto.encrypt(text));
    }

    @PostMapping("/crypto/decrypt")
    public Map<String, String> decrypt(@RequestParam String token) {
        return Map.of("text", crypto.decrypt(token));
    }

    // 5. 금액, 동시성 ----------------------------------------------------------
    @GetMapping("/interest")
    public Map<String, Object> interest(@RequestParam long principal, @RequestParam String rate) {
        return Map.of("monthlyInterest", loanService.monthlyInterestSafe(principal, rate));
    }

    @PostMapping("/loans/{loanId}/repay")
    public Map<String, Object> repay(@PathVariable long loanId, @RequestParam long amount, HttpSession session) {
        String me = LoginController.requireCustomer(session);
        return Map.of("success", loanService.repaySafe(loanId, me, amount));
    }
}
