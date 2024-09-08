package com.eco.ecoserver.domain.user.controller;

import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.dto.LoginResponseDto;
import com.eco.ecoserver.domain.user.dto.UserSignInDto;
import com.eco.ecoserver.domain.user.dto.UserUpdateDto;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.domain.user.dto.UserSignUpDto;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.jwt.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponseDto<User>> getUser(HttpServletRequest request) {
        Optional<String> email = jwtService.extrctEmailFromToken(request);
        if (email.isEmpty()) {
            return ResponseEntity.status(403).body(ApiResponseDto.failure(403, "권한이 없습니다."));
        }
        // TODO: email이 없는 경우 로그인이 안된 경우 (401), 권한이 없는 경우(ROLE 등) (403)

        Optional<User> user = userService.findByEmail(email.get());
        return user.map(value -> ResponseEntity.ok(ApiResponseDto.success(value)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponseDto.failure(404, "사용자를 찾을 수 없습니다.")));
    }

    @PutMapping("/me")
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

    @DeleteMapping("/me")
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

}
