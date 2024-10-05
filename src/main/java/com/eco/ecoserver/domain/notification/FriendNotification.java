package com.eco.ecoserver.domain.notification;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class FriendNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean view = false; // true if viewed, false otherwise

    @Column(nullable = false)
    private Long friendRequestListId;

    @Column(nullable = false)
    private Long requestUserId;

    @Column(nullable = false)
    private Long receiptUserId;

    private LocalDateTime createdAt;
    public FriendNotification() {
        this.createdAt = LocalDateTime.now();
    }
}
