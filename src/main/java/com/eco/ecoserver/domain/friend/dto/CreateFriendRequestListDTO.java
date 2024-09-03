package com.eco.ecoserver.domain.friend.dto;

import com.eco.ecoserver.domain.friend.FriendRequestList;
import com.eco.ecoserver.domain.friend.FriendState;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateFriendRequestListDTO {
    private Long userId;
    private Long friendId;
    private FriendState friendState;

    public CreateFriendRequestListDTO(Long userId, Long friendId, FriendState friendState){
        this.userId = userId;
        this.friendId = friendId;
        this.friendState = friendState;
    }

    public FriendRequestList toEntity(){
        return FriendRequestList.builder()
                .userId(userId)
                .friendId(friendId)
                .friendState(friendState)
                .build();
    }
}
