package com.eco.ecoserver.global.login.controller;

import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.global.dto.TokenDto;
import com.eco.ecoserver.domain.user.dto.UserSignInDto;
import com.eco.ecoserver.domain.user.dto.UserSignUpDto;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.eco.ecoserver.global.login.service.LoginService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class LoginController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;

    private LoginService loginService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponseDto<String>> signUp(@RequestBody UserSignUpDto userSignUpDto) {
        try {
            loginService.signUp(userSignUpDto);
            return ResponseEntity.ok(ApiResponseDto.success("회원가입 성공"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.failure(e.getMessage()));
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponseDto<TokenDto>> signIn(@RequestBody UserSignInDto userSignInDto) {
        try {
            TokenDto tokenDto = loginService.signIn(userSignInDto);
            return ResponseEntity.ok(ApiResponseDto.success(tokenDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.failure(e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDto<?>> refreshAccessToken(@RequestBody TokenDto tokenDto) {
        try {
            TokenDto newTokenDto = loginService.refreshAccessToken(tokenDto);
            return ResponseEntity.ok(ApiResponseDto.success(newTokenDto));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, e.getMessage()));
        }
    }
}
