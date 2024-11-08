package com.eco.ecoserver.global.oauth2.handler;

import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Logger;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {
    private static final Logger log = Logger.getLogger(OAuth2LoginFailureHandler.class.getName());

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        ApiResponseDto<String> apiResponseDto = ApiResponseDto.failure(401, "소셜 로그인 실패!");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(apiResponseDto.toString());

        log.info("소셜 로그인에 실패했습니다. 에러 메시지 : " + exception.getMessage());
    }
}
