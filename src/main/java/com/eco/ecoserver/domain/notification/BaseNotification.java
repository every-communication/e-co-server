package com.eco.ecoserver.domain.notification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "notification_type")
public abstract class BaseNotification {

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

    // Getters and setters
}

