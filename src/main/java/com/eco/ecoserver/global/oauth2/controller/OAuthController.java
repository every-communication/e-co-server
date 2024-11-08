package com.eco.ecoserver.global.oauth2.controller;

import com.eco.ecoserver.domain.user.SocialType;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.oauth2.dto.OAuthRegistrationDto;
import com.eco.ecoserver.global.oauth2.service.KakaoLoginService;
import com.eco.ecoserver.global.oauth2.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class OAuthController {
    private final UserService userService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final KakaoLoginService kakaoLoginService;

    @GetMapping("/google")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/kakao")
    public void redirectToKakao(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/kakao");
    }

    @GetMapping("/oauth-id-validate/{socialId}")
    public boolean socialIdValid(@PathVariable String socialId, HttpServletResponse response) throws IOException {
        return userService.emailExists(socialId);
    }

    @PostMapping("/oauth-register/{socialId}")
    public ResponseEntity<ApiResponseDto<SocialType>> oauthRegister(
            @PathVariable("socialId") String id,
            @RequestBody OAuthRegistrationDto oauthRegistrationDto) throws Exception {
        try{
            User user = userService.oauthRegister(id, oauthRegistrationDto);
            return ResponseEntity.ok(ApiResponseDto.success(user.getSocialType()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponseDto.failure(e.getMessage()));
        }
    }
}
