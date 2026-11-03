package edu.secure.lab.web;

import edu.secure.lab.service.LoanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/** 안전 화면: 출력 인코딩(JSTL <c:out>, fn:escapeXml, JS textContent), CSRF 토큰 검증 */
@Controller
@RequestMapping("/safe")
public class SafePageController {

    private static final String CSRF_KEY = "csrfToken";
    private final SecureRandom random = new SecureRandom();
    private final LoanService loanService;

    public SafePageController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("q", q);
        return "safe-search";                       // JSTL <c:out> / fn:escapeXml 및 textContent → 이스케이프
    }

    @GetMapping("/repay-form")
    public String form(Model model, HttpSession session) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);
        session.setAttribute(CSRF_KEY, token);
        model.addAttribute("action", "/safe/repay-form");
        model.addAttribute("csrf", token);
        return "repay-form";
    }

    @PostMapping("/repay-form")
    @ResponseBody
    public String repay(@RequestParam long loanId, @RequestParam long amount,
                        @RequestParam(name = "_csrf", required = false) String csrf, HttpSession session) {
        String me = LoginController.requireCustomer(session);
        Object expected = session.getAttribute(CSRF_KEY);
        if (expected == null || csrf == null || !MessageDigest.isEqual(
                expected.toString().getBytes(StandardCharsets.UTF_8), csrf.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return "상환 처리: " + loanService.repaySafe(loanId, me, amount);
    }
}
