package com.eco.ecoserver.global.oauth2.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class KakaoTokenResponseDto {
    /**
     * tokenType: bearer로 고정
     */
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("access_token")
    private String accessToken;
    /**
     * 액세스 토큰과 ID 토큰의 만료 시간(초)
     */
    @JsonProperty("expires_in")
    private Integer expiresIn;
    @JsonProperty("refresh_token")
    private String refreshToken;
    /**
     * 리프레시 토큰 만료 시간(초)
     */
    @JsonProperty("refresh_token_expires_in")
    private Integer refreshTokenExpiresIn;

    @JsonProperty("id_token")
    private String idToken;
    @JsonProperty("scope")
    private String scope;
}
