package com.eco.ecoserver.domain.notification.repository;

import com.eco.ecoserver.domain.notification.VideoTelegraphyNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoTelegraphyNotificationRepository extends JpaRepository<VideoTelegraphyNotification, Long> {
    List<VideoTelegraphyNotification> findByRequestUserId(Long requestUserId);
}
