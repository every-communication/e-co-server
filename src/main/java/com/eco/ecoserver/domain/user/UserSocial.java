package com.eco.ecoserver.domain.user;


import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@Table(name = "USER_SOCIAL")
@AllArgsConstructor
public class UserSocial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_social_id")
    private Long id;

    private String socialId;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;


    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // User를 설정하는 메서드
    public void setUser(User user) {
        this.user = user;
        if (user.getUserSocial() != this) {
            user.setUserSocial(this);
        }
    }
}
