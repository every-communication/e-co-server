package com.eco.ecoserver.domain.user.controller;

import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.dto.LoginResponseDto;
import com.eco.ecoserver.domain.user.dto.UserSignInDto;
import com.eco.ecoserver.domain.user.dto.UserUpdateDto;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.domain.user.dto.UserSignUpDto;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.mysql.cj.log.Log;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    // 회원가입 (Create)
    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody UserSignUpDto userSignUpDto) {
        try {
            userService.signUp(userSignUpDto);
            return ResponseEntity.ok("회원가입 성공");
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 로그인(JWT 발급)
    @PostMapping("/sign-in")
    public ResponseEntity<LoginResponseDto> signIn(@RequestBody UserSignInDto userSignInDto) {
        if(userService.authenticate(userSignInDto.getEmail(), userSignInDto.getPassword())) {
            String token = jwtService.createAccessToken(userSignInDto.getEmail());
            return ResponseEntity.ok(new LoginResponseDto(token, "로그인 성공"));
        } else {
            return ResponseEntity.badRequest().body(new LoginResponseDto(null, "로그인 실패: 이메일 또는 비밀번호가 잘못되었습니다."));
        }
    }

    // 사용자 정보 조회 (Read)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 사용자 정보 수정 (Update)
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserUpdateDto userUpdateDto) {
        try {
            User updateUser = userService.updateUser(id, userUpdateDto);
            return ResponseEntity.ok(updateUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 사용자 삭제 (Delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("사용자 삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/jwt-test")
    public String jwtTest() {
        return "jwtTest 요청 성공";
    }


}
