package com.eco.ecoserver.domain.videotelephony.repository;

import com.eco.ecoserver.domain.videotelephony.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByCode(String code);

    @Query("SELECT r FROM Room r WHERE (r.ownerId = :userId OR r.friendId = :userId) AND r.deletedAt IS NOT NULL")
    List<Room> findByOwnerIdOrFriendIdAndDeletedAtIsNotNull(@Param("userId") Long userId);
}
