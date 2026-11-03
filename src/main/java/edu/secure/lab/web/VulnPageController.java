package edu.secure.lab.web;

import edu.secure.lab.service.LoanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** ⚠ 교육용 취약 화면: XSS(JSP EL ${q} 미이스케이프, JS innerHTML), CSRF(토큰 없음) */
@Controller
@RequestMapping("/vuln")
public class VulnPageController {

    private final LoanService loanService;

    public VulnPageController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("q", q);
        return "vuln-search";                       // JSP ${q} 및 innerHTML → 입력이 HTML/스크립트로 실행됨
    }

    @GetMapping("/repay-form")
    public String form(Model model) {
        model.addAttribute("action", "/vuln/repay-form");
        return "repay-form";                        // CSRF 토큰 없음
    }

    @PostMapping("/repay-form")
    @ResponseBody
    public String repay(@RequestParam long loanId, @RequestParam long amount, HttpSession session) {
        String me = LoginController.requireCustomer(session);
        return "상환 처리: " + loanService.repaySafe(loanId, me, amount);   // 세션만 믿고 처리
    }
}
