package com.eco.ecoserver.domain.friend.repository;

import com.eco.ecoserver.domain.friend.FriendRequestList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRequestListRepository extends JpaRepository<FriendRequestList, Long> {
    List<FriendRequestList> findByUserIdAndFriendId(Long userId, Long friendId);
    List<FriendRequestList> findByFriendId(Long userId);

    List<FriendRequestList> findByUserId(Long userId);
}
