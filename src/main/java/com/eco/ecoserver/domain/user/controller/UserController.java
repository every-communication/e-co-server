package com.eco.ecoserver.domain.user.controller;

import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.dto.LoginResponseDto;
import com.eco.ecoserver.domain.user.dto.UserSignInDto;
import com.eco.ecoserver.domain.user.dto.UserUpdateDto;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.domain.user.dto.UserSignUpDto;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.mysql.cj.log.Log;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/auth/sign-up")
    public ResponseEntity<ApiResponseDto<String>> signUp(@RequestBody UserSignUpDto userSignUpDto) {
        try {
            userService.signUp(userSignUpDto);
            return ResponseEntity.ok(ApiResponseDto.success("회원가입 성공"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.failure(e.getMessage()));
        }
    }

    @PostMapping("/auth/sign-in")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> signIn(@RequestBody UserSignInDto userSignInDto) {
        if (userService.authenticate(userSignInDto.getEmail(), userSignInDto.getPassword())) {
            String token = jwtService.createAccessToken(userSignInDto.getEmail());
            return ResponseEntity.ok(ApiResponseDto.success(new LoginResponseDto(token, "로그인 성공")));
        } else {
            return ResponseEntity.badRequest().body(ApiResponseDto.failure("로그인 실패: 이메일 또는 비밀번호가 잘못되었습니다."));
        }
    }

    @GetMapping("/users/me")
    public ResponseEntity<ApiResponseDto<User>> getUser(HttpServletRequest request) {
        Optional<String> email = jwtService.extrctEmailFromToken(request);
        if (email.isEmpty()) {
            return ResponseEntity.status(403).body(ApiResponseDto.failure(403, "권한이 없습니다."));
        }

        Optional<User> user = userService.findByEmail(email.get());
        return user.map(value -> ResponseEntity.ok(ApiResponseDto.success(value)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponseDto.failure(404, "사용자를 찾을 수 없습니다.")));
    }

    @PutMapping("/users/me")
    public ResponseEntity<ApiResponseDto<User>> updateUser(HttpServletRequest request, @RequestBody UserUpdateDto userUpdateDto) throws Exception {
        Optional<String> email = jwtService.extrctEmailFromToken(request);
        if (email.isEmpty()) {
            return ResponseEntity.status(403).body(ApiResponseDto.failure(403, "권한이 없습니다."));
        }

        Optional<User> user = userService.findByEmail(email.get());
        if (user.isPresent()) {
            User updatedUser = userService.updateUser(user.get().getId(), userUpdateDto);
            return ResponseEntity.ok(ApiResponseDto.success(updatedUser));
        } else {
            return ResponseEntity.status(404).body(ApiResponseDto.failure(404, "사용자를 찾을 수 없습니다."));
        }
    }

    @DeleteMapping("/users/me")
    public ResponseEntity<ApiResponseDto<String>> deleteUser(HttpServletRequest request) throws Exception {
        Optional<String> email = jwtService.extrctEmailFromToken(request);
        if (email.isEmpty()) {
            return ResponseEntity.status(403).body(ApiResponseDto.failure(403, "권한이 없습니다."));
        }

        Optional<User> user = userService.findByEmail(email.get());
        if (user.isPresent()) {
            userService.deleteUser(user.get().getId());
            return ResponseEntity.ok(ApiResponseDto.success("사용자 삭제 완료"));
        } else {
            return ResponseEntity.status(404).body(ApiResponseDto.failure(404, "사용자를 찾을 수 없습니다."));
        }
    }

    @GetMapping("/jwt-test")
    public String jwtTest() {
        return "jwtTest 요청 성공";
    }


}
