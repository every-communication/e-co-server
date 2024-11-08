package com.eco.ecoserver.global.oauth2.controller;

import com.eco.ecoserver.global.oauth2.CustomOAuth2User;
import com.eco.ecoserver.global.oauth2.service.KakaoLoginService;
import com.eco.ecoserver.global.oauth2.dto.KakaoTokenResponseDto;
import com.eco.ecoserver.global.oauth2.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class KakaoLoginController {
    private final KakaoLoginService kakaoLoginService;
    private final CustomOAuth2UserService customOAuth2UserService;

    public KakaoLoginController(KakaoLoginService kakaoLoginService, CustomOAuth2UserService customOAuth2UserService) {
        this.kakaoLoginService = kakaoLoginService;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @GetMapping("/auth/code/kakao")
    public void kakaoLogin(@RequestParam(value = "code", required = false) String code,
                           @RequestParam(value = "state", required = false) String state,
                           HttpServletResponse response) {

        try {
            log.info("kakao login - attempting to get token");
            KakaoTokenResponseDto kakaoToken = kakaoLoginService.getToken(code);

            OAuth2User oAuth2User = kakaoLoginService.getUserInfo(kakaoToken);
            log.info("kakao login - user: {}", oAuth2User);

            CustomOAuth2User customOAuth2User = (CustomOAuth2User) oAuth2User;

            if (kakaoLoginService.isGuest(customOAuth2User)) {
                customOAuth2UserService.handleGuestLogin(response, customOAuth2User);
            } else {
                customOAuth2UserService.loginSuccess(response, customOAuth2User);
            }

        } catch (Exception e) {
            log.error("Failed to login with Kakao =", e);
        }
    }
}
