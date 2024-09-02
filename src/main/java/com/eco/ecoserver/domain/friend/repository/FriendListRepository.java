package com.eco.ecoserver.domain.friend.repository;

import com.eco.ecoserver.domain.friend.FriendList;
import com.eco.ecoserver.domain.friend.FriendRequestList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendListRepository extends JpaRepository<FriendList, Long> {
   List<FriendList> findByUserIdAndFriendId(Long userId, Long friendId);

   List<FriendList> findByUserId(Long userId);
   void deleteByUserIdAndFriendId(Long userId, Long friendId);
}
