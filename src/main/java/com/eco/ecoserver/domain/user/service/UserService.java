package com.eco.ecoserver.domain.user.service;

import com.eco.ecoserver.domain.user.UserType;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.domain.user.dto.UserSignUpDto;
import com.eco.ecoserver.domain.user.Role;
import com.eco.ecoserver.domain.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signUp(UserSignUpDto userSignUpDto) throws Exception {

        if (userRepository.findByEmail(userSignUpDto.getEmail()).isPresent()) {
            throw new Exception("이미 존재하는 이메일입니다.");
        }

        if (userRepository.findByNickname(userSignUpDto.getNickname()).isPresent()) {
            throw new Exception("이미 존재하는 닉네임입니다.");
        }

        User user = User.builder()
                .email(userSignUpDto.getEmail())
                .password(userSignUpDto.getPassword())
                .nickname(userSignUpDto.getNickname())
                .role(Role.USER)
                .userType(UserType.NONDEAF)
                .build();

        user.passwordEncode(passwordEncoder);
        userRepository.save(user);
    }
}
