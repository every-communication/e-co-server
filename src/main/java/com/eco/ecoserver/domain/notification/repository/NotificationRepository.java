package com.eco.ecoserver.domain.notification.repository;

import com.eco.ecoserver.domain.notification.BaseNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<BaseNotification, Long> {
    List<BaseNotification> findByRequestUserId(Long requestUserId);
}
