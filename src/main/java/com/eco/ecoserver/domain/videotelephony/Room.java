package com.eco.ecoserver.domain.videotelephony;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(name = "user1_id")
    private Long user1Id;

    @Column(name = "user2_id")
    private Long user2Id;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "friend_id")
    private Long friendId;

    @Column(name = "mic1")
    private boolean mic1 = true;  // 초기값 true

    @Column(name = "cam1")
    private boolean cam1 = true;  // 초기값 true

    @Column(name = "mic2")
    private boolean mic2 = true;  // 초기값 true

    @Column(name = "cam2")
    private boolean cam2 = true;  // 초기값 true

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    @Builder
    public Room(Long id, String code, Long user1Id, Long user2Id, boolean mic1, boolean cam1, boolean mic2, boolean cam2 ){
        this.id = id;
        this.code = code;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.mic1 = mic1;
        this.cam1 = cam1;
        this.mic2 = mic2;
        this.cam2 = cam2;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public void setOwnerId(Long ownerId){
        this.ownerId = ownerId;
    }

    public void setFriendId(Long friendId){
        this.friendId = friendId;
    }

    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt){
        this.deletedAt = deletedAt;
    }


    public void updateUser1(Long userId){
        this.user1Id = userId;
    }
    public void updateUser2(Long userId){
        this.user2Id = userId;
    }

    public void updateMic1(boolean mic) {
        this.mic1 = mic;
    }

    public void updateCam1(boolean cam) {
        this.cam1 = cam;
    }

    public void updateMic2(boolean mic) {
        this.mic2 = mic;
    }

    public void updateCam2(boolean cam) {
        this.cam2 = cam;
    }
}