package com.eco.ecoserver.domain.user.service;

import com.eco.ecoserver.domain.user.SocialType;
import com.eco.ecoserver.domain.user.UserSocial;
import com.eco.ecoserver.domain.user.dto.UserInfoDto;
import com.eco.ecoserver.domain.user.dto.UserUpdateDto;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.domain.user.dto.UserSignUpDto;
import com.eco.ecoserver.domain.user.Role;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.repository.UserSocialRepository;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.eco.ecoserver.global.oauth2.dto.OAuthRegistrationDto;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSocialRepository userSocialRepository;
    private final JwtService jwtService;

    // 회원가입
    public void signUp(UserSignUpDto userSignUpDto) throws Exception {
        if (userRepository.findByEmail(userSignUpDto.getEmail()).isPresent()) {
            throw new Exception("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .email(userSignUpDto.getEmail())
                .password(userSignUpDto.getPassword())
                .nickname(userSignUpDto.getNickname())
                .thumbnail(userSignUpDto.getThumbnail())
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

    // 사용자 정보 업데이트
    public UserInfoDto updateUser(HttpServletRequest request, UserUpdateDto userUpdateDto) throws Exception {
        User user = findUserFromRequest(request)
                .orElseThrow(() -> new Exception("Unauthorized"));

        if(userUpdateDto.getNickname() != null) {
            user.setNickname(userUpdateDto.getNickname());
        }
        user.setUserType(userUpdateDto.getUserType());
        user.setThumbnail(userUpdateDto.getThumbnail());
        user = userRepository.save(user);

        return new UserInfoDto(user);
    }

    // 사용자 삭제
    public void deleteUser(HttpServletRequest  request) throws Exception {
        User user = findUserFromRequest(request)
                .orElseThrow(() -> new Exception("Unauthorized"));

        userRepository.delete(user);
    }

    // 사용자 인증 (로그인)
    public boolean authenticate(String email, String password) {
        return userRepository.findByEmail(email)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    // 이메일로 사용자 ID 조회
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public Long getUserIdByEmail(String email){
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public User oauthRegister(String email, OAuthRegistrationDto oauthRegistrationDto) throws Exception {
        Optional<User> userOptional = findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setThumbnail(oauthRegistrationDto.getThumbnail());
            user.setNickname(oauthRegistrationDto.getNickname());
            user.setUserType(oauthRegistrationDto.getUserType());
            user.authorizeUser(); // Change role to USER

            user = userRepository.save(user);

            return user;
        } else {
            throw new Exception("사용자를 찾을 수 없습니다.");
        }
    }

    public String getNicknameById(Long id){
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.map(User::getNickname).orElse(null);
    }

    public UserInfoDto getUser(HttpServletRequest request) throws Exception{
        User user = findUserFromRequest(request)
                .orElseThrow(() -> new Exception("Unauthorized"));
        return new UserInfoDto(user);
    }

    public Optional<User> findUserFromRequest(HttpServletRequest request){
        return jwtService.extractEmailFromToken(request)
                .flatMap(userRepository::findByEmail);
    }

    public String findEmailById(Long userId) {
        return userRepository.findById(userId)
                .map(User::getEmail)
                .orElse("Unknown");
    }
}
