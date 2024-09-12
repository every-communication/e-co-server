package com.eco.ecoserver.domain.user.controller;

import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.dto.UserInfoDto;
import com.eco.ecoserver.domain.user.dto.UserSignUpDto;
import com.eco.ecoserver.domain.user.dto.UserUpdateDto;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/users/me")
    public ResponseEntity<ApiResponseDto<UserInfoDto>> getUser(HttpServletRequest request) {
        // request(token)에서 email 추출
        Optional<String> email = jwtService.extractEmailFromToken(request);
        // TODO: email이 없는 경우 로그인이 안된 경우 (401), 권한이 없는 경우(ROLE 등) (403)
        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(403, "권한이 없습니다."));
        }

        // email로 찾은 user 반환
        Optional<User> user = userService.findByEmail(email.get());

        return user.map(value -> {
            UserInfoDto userInfoDto = new UserInfoDto(value);
            return ResponseEntity.ok(ApiResponseDto.success(userInfoDto));
        }).orElseGet(() -> ResponseEntity.status(404).body(ApiResponseDto.failure(404, "사용자를 찾을 수 없습니다.")));
    }

    @PutMapping("/users/me")
    public ResponseEntity<ApiResponseDto<UserInfoDto>> updateUser(HttpServletRequest request, @RequestBody UserUpdateDto userUpdateDto) throws Exception {
        // request(token)에서 email 추출
        Optional<String> email = jwtService.extractEmailFromToken(request);
        if (email.isEmpty()) {
            return ResponseEntity.status(403).body(ApiResponseDto.failure(403, "권한이 없습니다."));
        }

        // email로 찾은 user, update 후 info 반환
        Optional<User> user = userService.findByEmail(email.get());
        if (user.isPresent()) {
            UserInfoDto updatedUserInfo = userService.updateUser(user.get().getId(), userUpdateDto);
            return ResponseEntity.ok(ApiResponseDto.success(updatedUserInfo));
        } else {
            return ResponseEntity.status(404).body(ApiResponseDto.failure(404, "사용자를 찾을 수 없습니다."));
        }
    }

    @DeleteMapping("/users/me")
    public ResponseEntity<ApiResponseDto<String>> deleteUser(HttpServletRequest request) throws Exception {
        Optional<String> email = jwtService.extractEmailFromToken(request);
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

}
