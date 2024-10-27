package com.eco.ecoserver.global.oauth2.dto;

import com.eco.ecoserver.domain.user.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class OAuthRegistrationDto {
    private String thumbnail;
    private String nickname;
    private UserType userType;
}
