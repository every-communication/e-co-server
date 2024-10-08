package com.eco.ecoserver.domain.friend;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "FRIEND_REQUEST_LIST",
    // 복합 unique키 설정 (중복 저장 X)
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "friend_id", "friend_state"}
                )
        })
public class FriendRequestList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friend_request_list_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId; //요청 id

    @Column(name = "friend_id")
    private Long friendId;

    @Enumerated(EnumType.STRING)
    private FriendState friendState;

    @Builder
    public FriendRequestList(Long id, Long userId, Long friendId, FriendState friendState){
        this.id = id;
        this.userId = userId;
        this.friendId = friendId;
        this.friendState = friendState;
    }

    public void updateState(FriendState state){
        this.friendState = state;
    }
}
