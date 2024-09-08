package com.eco.ecoserver.domain.user.service;

import com.eco.ecoserver.domain.user.dto.UserUpdateDto;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.domain.user.dto.UserSignUpDto;
import com.eco.ecoserver.domain.user.Role;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.global.jwt.service.JwtService;
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

    // 회원가입
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
        userRepository.save(user);
    }

    // 사용자 정보 조회
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // 사용자 정보 업데이트
    public User updateUser(Long id, UserUpdateDto userUpdateDto) throws Exception {
        Optional<User> userOptional = userRepository.findById(id);
        if(userOptional.isPresent()) {
            User user = userOptional.get();
            user.setNickname(userUpdateDto.getNickname());
            user.setUserType(userUpdateDto.getUserType());
            if(userUpdateDto.getPassword() != null) {
                user.setPassword(passwordEncoder.encode(userUpdateDto.getPassword()));
            }
            return userRepository.save(user);
        } else {
            throw new Exception("사용자를 찾을 수 없습니다.");
        }
    }

    // 사용자 삭제
    public void deleteUser(Long id) throws Exception {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new Exception("사용자를 찾을 수 없습니다.");
        }
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

    public Long getUserIdByEmail(String email){
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

}
