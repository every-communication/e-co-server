package com.eco.ecoserver.global.jwt.filter;

import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.eco.ecoserver.global.jwt.util.PasswordUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

/**
 * Jwt 인증 필터
 * "/auth/sign-in" (로그인) 이외의 URI 요청이 왔을 때 처리하는 필터
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {
    private static final Set<String> NO_CHECK_URL = new HashSet<>() {{
        add("/");
        add("/css");
        add("/images");
        add("/js");
        add("/favicon.ico");
        add("/index.html");
        add("/api-docs");
        add("/swagger-ui");
        add("/auth");
        add("/login");
        add("/oauth2");
        add("/health");
        add("/signal");
    }};

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        log.info("Request URI: {}", uri);

        // NO_CHECK_PATHS와 일치하는 경우에만 필터를 건너뛰도록 수정
        if (NO_CHECK_URL.stream().anyMatch(path -> uri.equals(path) || uri.startsWith(path + "/"))) {
            filterChain.doFilter(request, response);
            log.info("skip URI : {}", uri);
            return;
        }

        // 여기서부터는 모든 요청에 대해 JWT 체크 수행
        log.info("JWT check for URI : {}", uri);

        // Request Header 에서 Authorization 추출
        String authHeader = request.getHeader("Authorization");
        log.info("Authorization Header: {}", authHeader);

        // 사용자 요청 헤더에서 RefreshToken 추출
        String refreshToken = jwtService.extractRefreshToken(request)
                .filter(jwtService::isTokenValid)
                .orElse(null);

        if (refreshToken == null) {
            log.info("No valid refresh token found. Checking access token and authentication.");
            checkAccessTokenAndAuthentication(request, response, filterChain);
            return; // 여기서 return 추가
        }

        // refresh token이 있는 경우
        checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
        log.info("Valid refresh token found. Checking user and re-issuing access token.");
    }

    /**
     *  refresh token == user.refresh_token -> reIssue -> response(Token Dto)
     */
    public void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) {
        userRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> {
                    String reIssuedRefreshToken = reIssueRefreshToken(user);
                    String accessToken = jwtService.createAccessToken(user.getEmail());
                    jwtService.sendAccessAndRefreshToken(response, accessToken, reIssuedRefreshToken);
                });
    }

    /**
     * create refresh token -> update refresh token -> save & flush
     */
    private String reIssueRefreshToken(User user) {
        String reIssuedRefreshToken = jwtService.createRefreshToken(user.getEmail());
        user.updateRefreshToken(reIssuedRefreshToken);
        userRepository.saveAndFlush(user);
        return reIssuedRefreshToken;
    }

    /**
     * Access Token 유효성 검사 -> email 체크 -> saveAuthentication -> 다음 필터로 이동
     */
    public void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                  FilterChain filterChain) throws ServletException, IOException {
        // AccessToken 추출 (log 확인용)
        Optional<String> accessTokenOptional = jwtService.extractAccessToken(request);
        log.info("Extracted Access Token: {}", accessTokenOptional.orElse("No Access Token"));

        // access token 추출 -> filter (isTokenValid 를 통해 유효한지 확인) -> access token 으로 email 추출
        // -> email로 user 확인 -> saveAuthentication 호출 (내장 메소드)
        if(accessTokenOptional.isPresent()) {
            String accessToken = accessTokenOptional.get();

            if(!jwtService.isTokenValid(accessToken)) {
                // 유효하지 않은 토큰 응답
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 상태 코드

                ApiResponseDto<String> apiResponse = ApiResponseDto.failure(401, "Unauthorized");
                response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                response.getWriter().flush();
                response.getWriter().close();
                return; // 필터 체인 중단
            }

            // 유효한 경우 이메일 추출 및 사용자 확인
            jwtService.extractEmail(accessToken)
                    .ifPresent(email -> userRepository.findByEmail(email)
                            .ifPresent(this::saveAuthentication));
        }
        // 다음 인증 필터로 이동.
        filterChain.doFilter(request, response);
    }

    public void saveAuthentication(User myUser) {
        String password = myUser.getPassword();
        if (password == null) { // 소셜 로그인 유저의 비밀번호 임의로 설정 하여 소셜 로그인 유저도 인증 되도록 설정
            password = PasswordUtil.generateRandomPassword();
        }

        UserDetails userDetailsUser = org.springframework.security.core.userdetails.User.builder()
                .username(myUser.getEmail())
                .password(password)
                .roles(myUser.getRole().name())
                .build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetailsUser, null,
                        authoritiesMapper.mapAuthorities(userDetailsUser.getAuthorities()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
