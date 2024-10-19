package com.eco.ecoserver.domain.videotelephony.repository;

import com.eco.ecoserver.domain.videotelephony.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByCode(String code);
}
