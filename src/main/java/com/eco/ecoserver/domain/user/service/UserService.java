package com.eco.ecoserver.domain.user.service;

import com.eco.ecoserver.domain.friend.exception.UserNotFoundException;
import com.eco.ecoserver.domain.user.SocialType;
import com.eco.ecoserver.domain.user.UserSocial;
import com.eco.ecoserver.domain.user.dto.UserInfoDto;
import com.eco.ecoserver.domain.user.dto.UserUpdateDto;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.domain.user.dto.UserSignUpDto;
import com.eco.ecoserver.domain.user.Role;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.repository.UserSocialRepository;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.eco.ecoserver.global.oauth2.dto.OAuthRegistrationDto;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserInfoDto getUser(HttpServletRequest request) throws Exception {
        User user = findUserFromRequest(request)
                .orElseThrow(() -> new Exception("Unauthorized"));
        return new UserInfoDto(user);
    }

    public UserInfoDto updateUser(HttpServletRequest request, UserUpdateDto userUpdateDto) throws Exception {
        User user = findUserFromRequest(request)
                .orElseThrow(() -> new Exception("Unauthorized"));

        if(userUpdateDto.getNickname() != null) {
            user.setNickname(userUpdateDto.getNickname());
        }
        if(userUpdateDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userUpdateDto.getPassword()));
        }
        user.setUserType(userUpdateDto.getUserType());
        user = userRepository.save(user);

        return new UserInfoDto(user);
    }

    public void deleteUser(HttpServletRequest request) throws Exception {
        User user = findUserFromRequest(request)
                .orElseThrow(() -> new Exception("Unauthorized"));

        userRepository.delete(user);
    }

    public Optional<User> findUserFromRequest(HttpServletRequest request) {
        return jwtService.extractEmailFromToken(request)
                .flatMap(userRepository::findByEmail);
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
