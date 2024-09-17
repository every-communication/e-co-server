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
            userService.signUp(userSignUpDto);
            return ResponseEntity.ok(ApiResponseDto.success("회원가입 성공"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.failure(e.getMessage()));
        }
    }


    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponseDto<TokenDto>> signIn(@RequestBody UserSignInDto userSignInDto) {
        System.out.println(userSignInDto);

        if (userService.authenticate(userSignInDto.getEmail(), userSignInDto.getPassword())) {
            String accessToken = jwtService.createAccessToken(userSignInDto.getEmail());
            String refreshToken = jwtService.createRefreshToken();

            Optional<User> userOptional = userRepository.findByEmail(userSignInDto.getEmail());

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.updateRefreshToken(refreshToken);

                // 로그 추가
                log.info("Updating refresh token for user: {}", user.getEmail());
                log.info("New refresh token: {}", refreshToken);

                userRepository.saveAndFlush(user);

                // 로그 추가
                log.info("User after update: {}", user);
            } else {
                return ResponseEntity.badRequest().body(ApiResponseDto.failure("로그인 실패: 유저를 찾을 수 없습니다."));
            }

            return ResponseEntity.ok(ApiResponseDto.success(new TokenDto(accessToken, refreshToken)));
        } else {
            return ResponseEntity.badRequest().body(ApiResponseDto.failure("로그인 실패: 이메일 또는 비밀번호가 잘못되었습니다."));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDto<?>> refreshAccessToken(@RequestBody TokenDto tokenDto) {
        String refreshToken = tokenDto.getRefreshToken();

        if(jwtService.isTokenValid(tokenDto.getRefreshToken())) {
            Optional<String> email = jwtService.extractEmail(tokenDto.getAccessToken());

            if(email.isPresent()) {
                String newAccessToken = jwtService.createAccessToken(email.get());
                return ResponseEntity.ok(ApiResponseDto.success(new TokenDto(newAccessToken, refreshToken)));
            }
            else {
                return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "액세스 토큰에서 이메일을 추출할 수 없습니다."));
            }
        }
        else {
            return ResponseEntity.status(403).body(ApiResponseDto.failure("유효하지 않은 refresh token입니다."));
        }
    }

    @GetMapping("/jwt-test")
    public String jwtTest() {
        return "jwtTest 요청 성공";
    }
}
