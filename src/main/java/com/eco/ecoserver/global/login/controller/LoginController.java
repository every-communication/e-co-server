package com.eco.ecoserver.global.login.controller;

import com.eco.ecoserver.domain.user.dto.TokenDto;
import com.eco.ecoserver.domain.user.dto.UserSignInDto;
import com.eco.ecoserver.domain.user.dto.UserSignUpDto;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.eco.ecoserver.global.login.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
    public ResponseEntity<ApiResponseDto<TokenDto>> signIn(@RequestBody UserSignInDto userSignInDto) {
        if (userService.authenticate(userSignInDto.getEmail(), userSignInDto.getPassword())) {
            String accessToken = jwtService.createAccessToken(userSignInDto.getEmail());
            // TODO: refresh token 제공 로직 수정
            String refreshToken = jwtService.createRefreshToken();
            return ResponseEntity.ok(ApiResponseDto.success(new TokenDto(accessToken, refreshToken)));
        } else {
            return ResponseEntity.badRequest().body(ApiResponseDto.failure("로그인 실패: 이메일 또는 비밀번호가 잘못되었습니다."));
        }
    }

    /**
     * refresh token 전달 받으면 db와 비교 검증
     * 일치할 경우 진행 => 새 access token / refresh token 발급 (저장)
     * validateRefreshToken 메소드 수정...
     * */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDto<?>> refreshAccessToken(@RequestBody String refreshToken) {
        if (jwtService.validateRefreshToken(refreshToken)) {
            String email = jwtService.getEmailFromRefreshToken(refreshToken);
            String newAccessToken = jwtService.createAccessToken(email);
            return ResponseEntity.ok(ApiResponseDto.success(new TokenDto(newAccessToken, newRefreshToken)));
        } else {
            return ResponseEntity.status(403).body(ApiResponseDto.failure("유효하지 않은 refresh token입니다."));
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
