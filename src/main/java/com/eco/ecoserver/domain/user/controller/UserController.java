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
        try {
            UserInfoDto userInfoDto = userService.getUser(request);
            return ResponseEntity.ok(ApiResponseDto.success(userInfoDto));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, e.getMessage()));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponseDto<UserInfoDto>> updateUser(HttpServletRequest request, @RequestBody UserUpdateDto userUpdateDto) throws Exception {
        try {
            UserInfoDto userInfoDto = userService.updateUser(request, userUpdateDto);
            return ResponseEntity.ok(ApiResponseDto.success(userInfoDto));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, e.getMessage()));
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponseDto<String>> deleteUser(HttpServletRequest request) throws Exception {
        try {
            userService.deleteUser(request);
            return ResponseEntity.ok(ApiResponseDto.success("사용자 삭제 완료"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, e.getMessage()));
        }
    }
}
