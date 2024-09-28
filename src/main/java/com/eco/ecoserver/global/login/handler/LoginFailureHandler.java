package com.eco.ecoserver.global.login.handler;

import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 로그인 실패 시 처리하는 핸들러
 * SimpleUrlAuthenticationFailureHandler를 상속받아서 구현
 */
@Slf4j
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        ApiResponseDto<String> errorResponse = ApiResponseDto.failure("로그인 실패: 이메일 또는 비밀번호가 잘못되었습니다.");

        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        ObjectMapper objectMapper = new ObjectMapper(); // ObjectMapper 인스턴스 생성
        objectMapper.writeValue(response.getWriter(), errorResponse);

        log.info("로그인에 실패했습니다. 메시지 : {}", exception.getMessage());
    }
}
