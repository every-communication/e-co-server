package com.eco.ecoserver.domain.videotelephony.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoomDto {
    private Long id;
    private String code;
    private Long user1Id;
    private Long user2Id;
    private boolean mic1;
    private boolean cam1;
    private boolean mic2;
    private boolean cam2;

}
