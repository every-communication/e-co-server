package com.eco.ecoserver.domain.friend.repository;

import com.eco.ecoserver.domain.friend.FriendRequestList;
import com.eco.ecoserver.domain.friend.FriendState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRequestListRepository extends JpaRepository<FriendRequestList, Long> {
    List<FriendRequestList> findByUserIdAndFriendId(Long userId, Long friendId);
    List<FriendRequestList> findByFriendIdAndFriendState(Long userId, FriendState state);

    List<FriendRequestList> findByUserIdAndFriendState(Long userId, FriendState state);
    List<FriendRequestList> findByUserId(Long userId);
}
