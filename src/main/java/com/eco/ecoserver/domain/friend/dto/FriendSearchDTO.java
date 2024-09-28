package com.eco.ecoserver.domain.friend.dto;

import com.eco.ecoserver.domain.friend.FriendState;
import com.eco.ecoserver.domain.friend.FriendType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@NoArgsConstructor
public class FriendSearchDTO {
    private Long userId;
    private String email;
    private String nickname;
    private String thumbnail;
    private FriendType friendType;

    public FriendSearchDTO(Long userId, String email, String nickname, String thumbnail, FriendType friendType){
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.thumbnail = thumbnail;
        this.friendType = friendType;
    }

}
