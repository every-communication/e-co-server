package com.eco.ecoserver.domain.friend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FriendListDTO {
    private Long userId;
    private String email;
    private String nickname;
    private String thumbnail;

    public FriendListDTO(Long userId, String email, String nickname, String thumbnail){
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.thumbnail = thumbnail;
    }


}
