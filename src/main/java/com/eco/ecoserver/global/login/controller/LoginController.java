package com.eco.ecoserver.global.login.controller;

import com.eco.ecoserver.domain.user.controller.UserController;
import com.eco.ecoserver.domain.user.dto.LoginResponseDto;
import com.eco.ecoserver.domain.user.dto.UserSignInDto;
import com.eco.ecoserver.domain.user.dto.UserSignUpDto;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.eco.ecoserver.global.login.service.LoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class LoginController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;

    private LoginService loginService;

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponseDto<String>> signUp(@RequestBody UserSignUpDto userSignUpDto) {
        try {
            userService.signUp(userSignUpDto);
            return ResponseEntity.ok(ApiResponseDto.success("회원가입 성공"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.failure(e.getMessage()));
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> signIn(@RequestBody UserSignInDto userSignInDto) {
        if (userService.authenticate(userSignInDto.getEmail(), userSignInDto.getPassword())) {
            String accessToken = jwtService.createAccessToken(userSignInDto.getEmail());
            // TODO: refresh token 제공 로직 수정
            String refreshToken = jwtService.createRefreshToken();
            return ResponseEntity.ok(ApiResponseDto.success(new LoginResponseDto(accessToken, refreshToken, "로그인 성공")));
        } else {
            return ResponseEntity.badRequest().body(ApiResponseDto.failure("로그인 실패: 이메일 또는 비밀번호가 잘못되었습니다."));
        }
    }

    @GetMapping("/naver")
    public String redirectToNaver() {
        return "redirect:/oauth2/authorization/naver";
    }

    @GetMapping("/google")
    public String redirectToGoogle() {
        return "redirect:/oauth2/authorization/google";
    }

    @GetMapping("/kakao")
    public String redirectToKakao() {
        return "redirect:/oauth2/authorization/kakao";
    }


    @GetMapping("/jwt-test")
    public String jwtTest() {
        return "jwtTest 요청 성공";
    }
}
