package com.eco.ecoserver.global.login.service;

import com.eco.ecoserver.domain.user.Role;
import com.eco.ecoserver.domain.user.SocialType;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.UserSocial;
import com.eco.ecoserver.domain.user.dto.UserSignInDto;
import com.eco.ecoserver.domain.user.dto.UserSignUpDto;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.domain.user.repository.UserSocialRepository;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.dto.TokenDto;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.eco.ecoserver.global.oauth2.dto.OAuthRegistrationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSocialRepository userSocialRepository;
    private final JwtService jwtService;

    public void signUp(UserSignUpDto userSignUpDto) throws Exception {
        if (userRepository.findByEmail(userSignUpDto.getEmail()).isPresent()) {
            throw new Exception("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .email(userSignUpDto.getEmail())
                .password(userSignUpDto.getPassword())
                .nickname(userSignUpDto.getNickname())
                .role(Role.USER)
                .userType(userSignUpDto.getUserType())
                .build();

        user.passwordEncode(passwordEncoder);
        User savedUser = userRepository.save(user);

        UserSocial userSocial = UserSocial.builder()
                .user(savedUser)
                .socialType(SocialType.ECO)
                .socialId(savedUser.getEmail())
                .build();

        userSocialRepository.save(userSocial);
    }

    public TokenDto signIn(UserSignInDto userSignInDto) throws Exception {
        if(authenticate(userSignInDto.getEmail(), userSignInDto.getPassword())) {
            String accessToken = jwtService.createAccessToken(userSignInDto.getEmail());
            String refreshToken = jwtService.createRefreshToken(userSignInDto.getEmail());

            Optional<User> userOpt = userRepository.findByEmail(userSignInDto.getEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.updateRefreshToken(refreshToken);
                userRepository.saveAndFlush(user);
                return new TokenDto(accessToken, refreshToken);
            }
            throw new Exception("로그인 실패: 유저를 찾을 수 없습니다.");
        }
        throw new Exception("로그인 실패: 이메일 또는 비밀번호가 잘못되었습니다.");
    }

    public TokenDto refreshAccessToken(TokenDto tokenDto) throws Exception {
        String refreshToken = tokenDto.getRefreshToken();

        if(!jwtService.isTokenValid(refreshToken)) {
            throw new Exception("Unauthorized");
        }

        return jwtService.extractEmail(refreshToken)
                .map(email -> {
                    String newAccessToken = jwtService.createAccessToken(email);
                    return new TokenDto(newAccessToken, refreshToken);
                })
                .orElseThrow(() -> new Exception("Unauthorized"));
    }

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

    public boolean authenticate(String email, String password) {
        return userRepository.findByEmail(email)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    public User oauthRegister(String email, OAuthRegistrationDto oauthRegistrationDto) throws Exception {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setNickname(oauthRegistrationDto.getNickname());
            user.setUserType(oauthRegistrationDto.getUserType());
            user.authorizeUser(); // Change role to USER

            user = userRepository.save(user);

            return user;
        } else {
            throw new Exception("사용자를 찾을 수 없습니다.");
        }
    }
}
