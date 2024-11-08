package com.eco.ecoserver.global.login.service;

import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.jwt.dto.TokenDto;
import com.eco.ecoserver.global.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 사용자 인증 정보를 로드하는 서비스
 * UserDetailsService를 구현하여 사용자 정보 로드
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일이 존재하지 않습니다."));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    public ResponseEntity<ApiResponseDto<TokenDto>> refreshTokenResponse(String refreshToken) {
        Optional<String> emailOpt = jwtService.extractEmail(refreshToken);
        if (emailOpt.isPresent()) {
            String email = emailOpt.get();

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                if (user.getRefreshToken().equals(refreshToken)) {
                    String newAccessToken = jwtService.createAccessToken(email);
                    String newRefreshToken = jwtService.createRefreshToken(email);

                    user.updateRefreshToken(newRefreshToken);
                    log.info("Updating refresh token for user: {}", email);
                    userRepository.saveAndFlush(user);
                    return ResponseEntity.ok(ApiResponseDto.success(new TokenDto(newAccessToken, newRefreshToken)));
                }
            }
        }
        return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorized"));
    }

    public ApiResponseDto<TokenDto> loginSuccessToken(String email) {
        String accessToken = jwtService.createAccessToken(email);
        String refreshToken = jwtService.createRefreshToken(email);

        return userRepository.findByEmail(email)
                .map(user -> {
                    user.updateRefreshToken(refreshToken);
                    log.info("Updating refresh token for user: {}", email);
                    userRepository.saveAndFlush(user);
                    return ApiResponseDto.success(new TokenDto(accessToken, refreshToken));
                })
                .orElseGet(() -> ApiResponseDto.failure(401, "로그인 실패: 유저를 찾을 수 없습니다."));
    }
}
