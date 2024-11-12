package com.eco.ecoserver.domain.videotelephony.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CallInfoDto {
    private Long friendId;

    private String friendName;
    private String friendEmail;
    private String friendThumbnail;
    private boolean friendOrNot;
    private long duration;
    private String deletedAt;
}
