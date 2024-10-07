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
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponseDto<UserInfoDto>> getUser(HttpServletRequest request) {
        // request(token)에서 email 추출
        Optional<String> email = jwtService.extractEmailFromToken(request);
        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed"));
        }

        // email로 찾은 user 반환
        Optional<User> user = userService.findByEmail(email.get());

        return user.map(value -> {
            UserInfoDto userInfoDto = new UserInfoDto(value);
            return ResponseEntity.ok(ApiResponseDto.success(userInfoDto));
        }).orElseGet(() -> ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed")));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponseDto<UserInfoDto>> updateUser(HttpServletRequest request, @RequestBody UserUpdateDto userUpdateDto) throws Exception {
        // request(token)에서 email 추출
        Optional<String> email = jwtService.extractEmailFromToken(request);
        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed"));
        }

        // email로 찾은 user, update 후 info 반환
        Optional<User> user = userService.findByEmail(email.get());
        if (user.isPresent()) {
            UserInfoDto updatedUserInfo = userService.updateUser(user.get().getId(), userUpdateDto);
            return ResponseEntity.ok(ApiResponseDto.success(updatedUserInfo));
        } else {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed"));
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponseDto<String>> deleteUser(HttpServletRequest request) throws Exception {
        Optional<String> email = jwtService.extractEmailFromToken(request);
        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed"));
        }

        Optional<User> user = userService.findByEmail(email.get());
        if (user.isPresent()) {
            userService.deleteUser(user.get().getId());
            return ResponseEntity.ok(ApiResponseDto.success("사용자 삭제 완료"));
        } else {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed"));
        }
    }
}
