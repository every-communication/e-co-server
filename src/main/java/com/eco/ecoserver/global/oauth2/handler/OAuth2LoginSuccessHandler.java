package com.eco.ecoserver.global.oauth2.handler;

import com.eco.ecoserver.domain.user.dto.TokenDto;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.eco.ecoserver.global.oauth2.CustomOAuth2User;
import com.eco.ecoserver.domain.user.Role;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Logger;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = Logger.getLogger(OAuth2LoginSuccessHandler.class.getName());
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    // 생성자에서 ObjectMapper를 주입
    public OAuth2LoginSuccessHandler(JwtService jwtService, ObjectMapper objectMapper, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper; // ObjectMapper 주입
        this.userRepository = userRepository;
    }

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login 성공!");
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        if (oAuth2User.getRole() == Role.GUEST) {
            handleGeustLogin(response, oAuth2User);
        } else {
            loginSuccess(response, oAuth2User);
        }
    }

    private void handleGeustLogin(HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException {
        // GUEST일 경우 access token 발급 후 리다이렉트
        String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());

        // 리다이렉트 URL에 email 정보 포함 (필요시 id 등을 추가)
        String redirectUrl = "/auth/oauth-register?id=" + oAuth2User.getEmail();
        response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
        response.sendRedirect(redirectUrl);
    }

    private void loginSuccess(HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException {
        String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());
        String refreshToken = jwtService.createRefreshToken();

        TokenDto tokenDto = new TokenDto(accessToken, refreshToken);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String tokenJson = objectMapper.writeValueAsString(tokenDto);
        response.getWriter().write(tokenJson);

        userRepository.findByEmail(oAuth2User.getEmail())
                        .ifPresent(user -> {
                            user.updateRefreshToken(refreshToken);
                            userRepository.saveAndFlush(user);
                        });
        /* 기존 코드
        response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
        response.addHeader(jwtService.getRefreshHeader(), "Bearer " + refreshToken);
        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        jwtService.updateRefreshToken(oAuth2User.getEmail(), refreshToken);
         */
    }
}
