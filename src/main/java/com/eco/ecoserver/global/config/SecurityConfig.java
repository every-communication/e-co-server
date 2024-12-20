package com.eco.ecoserver.global.config;

import com.eco.ecoserver.global.cors.filter.CustomCorsFilter;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.global.jwt.filter.JwtAuthenticationProcessingFilter;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.eco.ecoserver.global.login.filter.CustomJsonUsernamePasswordAuthenticationFilter;
import com.eco.ecoserver.global.login.handler.LoginFailureHandler;
import com.eco.ecoserver.global.login.handler.LoginSuccessHandler;
import com.eco.ecoserver.global.login.service.LoginService;
import com.eco.ecoserver.global.oauth2.handler.OAuth2LoginFailureHandler;
import com.eco.ecoserver.global.oauth2.handler.OAuth2LoginSuccessHandler;
import com.eco.ecoserver.global.oauth2.service.CustomOAuth2UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import org.slf4j.Logger;
import org.springframework.util.unit.DataSize;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 인증은 CustomJsonUsernamePasswordAuthenticationFilter에서 authenticate()로 인증된 사용자로 처리
 * JwtAuthenticationProcessingFilter는 AccessToken, RefreshToken 재발급
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginService loginService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;


    @Value("${spring.servlet.multipart.max-file-size}")
    private long maxFileSize; // 최대 파일 크기 (바이트 단위)


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                //== URL별 권한 관리 옵션 ==//
                // 아이콘, css, js 관련
                // 기본 페이지, css, image, js 하위 폴더에 있는 자료들은 모두 접근 가능
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/images/**","/js/**", "/favicon.ico", "/index.html").permitAll()
                        .requestMatchers("/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers("/auth/**", "/login/**", "/oauth2/**", "/health", "/favicon.ico/**").permitAll()
                        .requestMatchers("/signal").permitAll()
                        .anyRequest().authenticated() // 위의 경로 이외에는 모두 인증된 사용자만 접근 가능
                )
                .cors(cors -> cors.disable()) // 기본 CORS 설정 비활성화
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .httpBasic(httpBasic -> httpBasic.disable()) // HTTP Basic 비활성화
                .formLogin(form -> form.disable()) // Form 로그인 비활성화
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())) // FrameOptions 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용하지 않으므로 STATELESS로 설정

                //== 소셜 로그인 설정 ==//
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler) // 동의하고 계속하기를 눌렀을 때 Handler 설정
                        .failureHandler(oAuth2LoginFailureHandler) // 소셜 로그인 실패 시 핸들러 설정
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)) // customUserService 설정
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> {
                            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                );

        // CORS 필터
        //http.addFilterBefore(new CustomCorsFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new CustomCorsFilter(), ChannelProcessingFilter.class); // 가장 앞에 CustomCorsFilter 추가
        http.addFilterBefore(new CorsFilter(corsConfigurationSource()), UsernamePasswordAuthenticationFilter.class);

        //file size check
        http.addFilterBefore(new FileSizeCheckFilter(maxFileSize), UsernamePasswordAuthenticationFilter.class);

        // JWT 필터
        http.addFilterAfter(customJsonUsernamePasswordAuthenticationFilter(), LogoutFilter.class);
        http.addFilterBefore(jwtAuthenticationProcessingFilter(), CustomJsonUsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "https://api.e-co.rldnd.net",
                "https://api.e-co.rldnd.net",
                "ws://localhost:8080",
                "wss://api.e-co.rldnd.net"
        )); // 허용할 도메인
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTION")); // 허용할 HTTP 메서드
        configuration.setAllowedHeaders(Arrays.asList("*")); // 허용할 헤더
        configuration.setAllowCredentials(true); // 자격 증명 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 CORS 설정 적용
        return source;
    }

    public void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        ApiResponseDto<String> errorResponse = ApiResponseDto.failure(status, message);

        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(status);

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
        response.getWriter().close();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(loginService);
        return new ProviderManager(provider);
    }

    /**
     * 로그인 성공 시 호출되는 LoginSuccessJWTProviderHandler 빈 등록
     */
    @Bean
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler(jwtService, userRepository, loginService, objectMapper);
    }

    /**
     * 로그인 실패 시 호출되는 LoginFailureHandler 빈 등록
     */
    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler(loginService, objectMapper);
    }

    @Bean
    public CustomJsonUsernamePasswordAuthenticationFilter customJsonUsernamePasswordAuthenticationFilter() {
        CustomJsonUsernamePasswordAuthenticationFilter customJsonUsernamePasswordLoginFilter
                = new CustomJsonUsernamePasswordAuthenticationFilter(objectMapper);
        customJsonUsernamePasswordLoginFilter.setAuthenticationManager(authenticationManager());
        customJsonUsernamePasswordLoginFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
        customJsonUsernamePasswordLoginFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return customJsonUsernamePasswordLoginFilter;
    }

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        return new JwtAuthenticationProcessingFilter(jwtService, userRepository, objectMapper);
    }


    public class FileSizeCheckFilter extends OncePerRequestFilter {
        private final Long maxFileSize;

        public FileSizeCheckFilter(Long maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            if (request.getContentLengthLong() > maxFileSize) {
                sendErrorResponse(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "File size exceeds the maximum limit of " + maxFileSize + "MB");
                return;
            }
            filterChain.doFilter(request, response);
        }
    }


}