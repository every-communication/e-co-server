package com.eco.ecoserver.domain.user.dto;

import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserInfoDto {
    private Long id;
    private String email;
    private String nickname;
    private String thumbnail;
    private UserType userType;

    // User 엔티티로부터 값을 받음
    public UserInfoDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.thumbnail = user.getThumbnail();
        this.userType = user.getUserType();
    }
}
