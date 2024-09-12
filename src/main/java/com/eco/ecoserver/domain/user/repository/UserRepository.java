package com.eco.ecoserver.domain.user.repository;

import com.eco.ecoserver.domain.user.SocialType;
import com.eco.ecoserver.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String nickname);
    Optional<User> findByEmail(String email);
    Optional<User> findByRefreshToken(String refreshToken);
}
