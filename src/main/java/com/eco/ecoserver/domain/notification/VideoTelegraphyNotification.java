package com.eco.ecoserver.domain.notification;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class VideoTelegraphyNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tupleId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean view; // true if viewed, false otherwise

    @Column(nullable = false)
    private Long friendRequestListId;

    @Column(nullable = false)
    private Long requestUserId;

    @Column(nullable = false)
    private Long receiptUserId;
}
