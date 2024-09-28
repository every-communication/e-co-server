package com.eco.ecoserver.domain.user.repository;

import com.eco.ecoserver.domain.user.SocialType;
import com.eco.ecoserver.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String nickname);
    Optional<User> findByEmail(String email);
    Optional<User> findByRefreshToken(String refreshToken);

    List<User> findByNicknameContainingIgnoreCase(String nickname);

    List<User> findByEmailContainingIgnoreCase(String email);
}
