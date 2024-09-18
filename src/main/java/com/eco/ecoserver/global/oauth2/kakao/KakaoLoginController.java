package com.eco.ecoserver.global.oauth2.kakao;

import com.eco.ecoserver.domain.user.SocialType;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.domain.user.repository.UserSocialRepository;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.eco.ecoserver.global.oauth2.CustomOAuth2User;
import com.eco.ecoserver.global.oauth2.OAuthAttributes;
import com.eco.ecoserver.global.oauth2.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

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
