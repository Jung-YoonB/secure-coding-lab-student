package edu.secure.lab.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginPageController {

    @GetMapping("/login-page")
    public String loginPage() {
        return "login";
    }
}
