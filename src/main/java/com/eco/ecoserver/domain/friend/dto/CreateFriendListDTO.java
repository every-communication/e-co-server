package com.eco.ecoserver.domain.friend.dto;

import com.eco.ecoserver.domain.friend.FriendList;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateFriendListDTO {
    private Long userId;
    private Long friendId;

    public CreateFriendListDTO(Long userId, Long friendId){
        this.userId = userId;
        this.friendId = friendId;
    }

    public FriendList toEntity(){
        return FriendList.builder()
                .userId(userId)
                .friendId(friendId)
                .build();
    }
}
