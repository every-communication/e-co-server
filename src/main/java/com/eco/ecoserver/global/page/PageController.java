package com.eco.ecoserver.global.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class PageController {
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // 추가 정보 페이지
    @GetMapping("/additional-info")
    public String additionalInfo() {
        return "additional-info"; // src/main/resources/templates/additional-info.html
    }

    // 로그인 페이지
    @GetMapping("/login-page")
    public String login() {
        return "login-page"; // src/main/resources/templates/login.html
    }

    // 회원가입 페이지
    @GetMapping("/register")
    public String register() {
        return "register"; // src/main/resources/templates/register.html
    }

    // 성공 페이지
    @GetMapping("/success")
    public String success() {
        return "success"; // src/main/resources/templates/success.html
    }

    // 실패 페이지
    @GetMapping("/failure")
    public String failure() {
        return "failure"; // src/main/resources/templates/failure.html
    }
    @GetMapping("/reg-success")
    public String regSuccess() {
        return "reg-success";
    }

    /*
    @GetMapping("/redirect-to-other-page")
    public RedirectView redirectToOtherPage() {
        return new RedirectView("/other-page");
    }
    */
}
