package com.eco.ecoserver.global.oauth2.handler;

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
        //TODO: ApiResponseDto 로 보내기
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write("소셜 로그인 실패! 서버 로그를 확인해주세요.");
        log.info("소셜 로그인에 실패했습니다. 에러 메시지 : " + exception.getMessage());
    }
}
