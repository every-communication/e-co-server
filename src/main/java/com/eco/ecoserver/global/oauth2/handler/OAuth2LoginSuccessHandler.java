package com.eco.ecoserver.global.oauth2.handler;

import com.eco.ecoserver.global.dto.TokenDto;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.eco.ecoserver.global.oauth2.CustomOAuth2User;
import com.eco.ecoserver.domain.user.Role;

import com.eco.ecoserver.global.oauth2.service.CustomOAuth2UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = Logger.getLogger(OAuth2LoginSuccessHandler.class.getName());
    private final CustomOAuth2UserService customOAuth2UserService;

    // 생성자에서 ObjectMapper를 주입
    public OAuth2LoginSuccessHandler(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login Success!");
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        if (oAuth2User.getRole() == Role.GUEST) {
            log.info("OAuth2 Login Success! - Guest");
            customOAuth2UserService.handleGuestLogin(response, oAuth2User);
        } else {
            log.info("OAuth2 Login Success! - User");
            customOAuth2UserService.loginSuccess(response, oAuth2User);
        }
    }
}
