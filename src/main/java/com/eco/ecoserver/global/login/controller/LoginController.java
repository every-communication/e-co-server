package com.eco.ecoserver.global.login.controller;

import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.global.jwt.dto.TokenDto;
import com.eco.ecoserver.domain.user.dto.UserSignInDto;
import com.eco.ecoserver.domain.user.dto.UserSignUpDto;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.eco.ecoserver.global.login.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    private LoginService loginService;
    @Autowired
    private UserRepository userRepository;

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
    public ResponseEntity<ApiResponseDto<TokenDto>> signIn(@RequestBody UserSignInDto userSignInDto) {
        if (userService.authenticate(userSignInDto.getEmail(), userSignInDto.getPassword())) {
            return loginService.generateTokenResponse(userSignInDto.getEmail());
        } else {
            return ResponseEntity.badRequest().body(ApiResponseDto.failure("로그인 실패: 이메일 또는 비밀번호가 잘못되었습니다."));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDto<TokenDto>> refreshAccessToken(@RequestBody TokenDto tokenDto) {
        if(jwtService.isTokenValid(tokenDto.getRefreshToken())) {
            return loginService.refreshTokenResponse(tokenDto.getRefreshToken());
        } else {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorized"));
        }
    }
}
