package com.eco.ecoserver.global.oauth2.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoUserInfoDto {
    private String id;
    @JsonProperty("connected_at")
    private String connectedAt;
    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    public class KakaoAccount {
        @Getter
        @JsonProperty("name_needs_agreement")
        private boolean nameNeedsAgreement;
        @JsonProperty("profile_nickname")
        private String profile_nickname;
        @JsonProperty("profile_image")
        private String profile_image;
        private String email;
        @JsonProperty("has_email")
        private boolean hasEmail;
        @JsonProperty("email_needs_agreement")
        private boolean emailNeedsAgreement;
        @JsonProperty("is_email_valid")
        private boolean isEmailValid;
        @JsonProperty("is_email_verified")
        private boolean isEmailVerified;
    }
}
