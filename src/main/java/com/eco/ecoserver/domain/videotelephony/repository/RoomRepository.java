package com.eco.ecoserver.domain.videotelephony.repository;

import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.videotelephony.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByCode(String code);
    List<Room> findByOwnerId(Long userId);
    List<Room> findByFriendId(Long userId);

}
