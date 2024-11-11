package com.eco.ecoserver.domain.videotelephony.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
public class RoomHistoryDto {
    private final Long friendId;
    private final String friendEmail;

    // 통화 시작 시간
    private final LocalDateTime createdAt;

    // 통화 진행 시간 (초 단위)
    private final long durationInSeconds;

    // 생성자
    public RoomHistoryDto(Long friendId, String friendEmail, LocalDateTime createdAt, LocalDateTime deletedAt) {
        this.friendId = friendId;
        this.friendEmail = friendEmail;
        this.createdAt = createdAt;

        if (createdAt != null && deletedAt != null) {
            this.durationInSeconds = ChronoUnit.SECONDS.between(createdAt, deletedAt);
        } else {
            this.durationInSeconds = 0;
        }
    }
}
