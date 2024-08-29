package com.eco.ecoserver.domain.user.dto;

import com.eco.ecoserver.domain.user.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UserSignUpDto {
    private String email;
    private String password;
    private String nickname;
    private UserType userType;
}
