package com.eco.ecoserver.domain.friend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FriendRequestDTO {
    private String searchUser;
    private Long requestId;


    public FriendRequestDTO(String searchUser, Long requestId){
        this.searchUser = searchUser;
        this.requestId = requestId;

    }

}
