package com.eco.ecoserver.domain.friend;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "FRIEND_LIST")
public class FriendList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friend_list_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "friend_id")
    private Long friendId;

    @Builder
    public FriendList(Long id, Long userId, Long friendId){
        this.id = id;
        this.userId = userId;
        this.friendId = friendId;
    }


}
