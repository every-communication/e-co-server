package com.eco.ecoserver.global.oauth2.kakao;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;


@Getter
@Setter
@Component
@PropertySource("classpath:application-oauth.yml")
public class KakaoLoginProperties {
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoLoginApiKey;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;


    private String kakaoAuthBaseUri ="";

    @Value("${spring.security.oauth2.client.provider.kakao.authorization-uri}")
    private String codeReqeustUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenRequestUri;

    private String kakaoApiBaseUri = "";

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String kakaoApiUserInfoRequestUri;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

}
