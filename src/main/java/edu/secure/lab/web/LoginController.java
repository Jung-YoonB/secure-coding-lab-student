package edu.secure.lab.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Set;

/**
 * 실습 편의를 위한 간이 로그인. 비밀번호 검증 없이 고객 ID 만으로 세션을 만든다.
 * 실제 서비스에서는 절대 이렇게 만들지 않는다.
 */
@RestController
public class LoginController {

    static final String SESSION_KEY = "customerId";
    private static final Set<String> DEMO_USERS = Set.of("C001", "C002", "C003");

    @PostMapping("/login")
    public Map<String, String> login(@RequestParam String customerId, HttpSession session) {
        if (!DEMO_USERS.contains(customerId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        session.setAttribute(SESSION_KEY, customerId);
        return Map.of("loggedInAs", customerId);
    }

    /** 세션의 고객 ID. 로그인하지 않았으면 401 */
    static String requireCustomer(HttpSession session) {
        Object id = session.getAttribute(SESSION_KEY);
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return id.toString();
    }
}
