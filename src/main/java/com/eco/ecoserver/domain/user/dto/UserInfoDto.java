package com.eco.ecoserver.domain.user.dto;

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
}
