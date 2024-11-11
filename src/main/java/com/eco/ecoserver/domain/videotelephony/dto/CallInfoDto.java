package com.eco.ecoserver.domain.videotelephony.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CallInfoDto {
    private String friendName;
    private String friendEmail;
    private String friendThumbnail;
    private boolean friendOrNot;
    private String totalCallTime;
    private String callTime;


}
