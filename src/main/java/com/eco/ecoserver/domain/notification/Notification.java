package com.eco.ecoserver.domain.notification;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    protected String message;

    @Column(nullable = false)
    private boolean view = false; // true if viewed, false otherwise

    @Column(nullable = false)
    private Long requestUserId;

    @Column(nullable = false)
    private Long receiptUserId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Setter
    @Enumerated(EnumType.STRING) // Enum 타입 저장
    @Column(nullable = false)
    private NotificationType notificationType;

    // 기본 생성자
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
