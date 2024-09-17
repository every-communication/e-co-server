package com.eco.ecoserver.domain.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@Table(name = "USERS")
@AllArgsConstructor
public class User {
    //UUID uuid = UUID.randomUUID();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String email;
    @Setter
    private String password;
    @Setter
    private String nickname;
    private String thumbnail; //imageUrl

    @Enumerated(EnumType.STRING)
    private Role role;

    @Setter
    @Enumerated(EnumType.STRING)
    private UserType userType;

    private String refreshToken;

    // 연관관계의 owner -> foreign key 생성하지 않고, 주인이 관리
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserSocial userSocial;

    // 양방향 연관 관계 설정 메서드 추가
    public void setUserSocial(UserSocial userSocial) {
        this.userSocial = userSocial;
        if (userSocial != null && userSocial.getUser() != this) {
            userSocial.setUser(this);
        }
    }

    public SocialType getSocialType() {
        return this.userSocial != null ? this.userSocial.getSocialType() : null;
    }

    // 유저 권한 설정 메소드
    public void authorizeUser() { this.role = Role.USER; }

    // 비밀번호 암호화 메소드
    public void passwordEncode(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    public void updateRefreshToken(String updateRefreshToken) {
        this.refreshToken = updateRefreshToken;
    }

}
