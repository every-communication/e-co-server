package com.eco.ecoserver.global.oauth2.kakao;

import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.domain.user.repository.UserSocialRepository;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.eco.ecoserver.global.oauth2.CustomOAuth2User;
import com.eco.ecoserver.global.oauth2.service.CustomOAuth2UserService;
import com.eco.ecoserver.global.oauth2.service.OAuthImageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;

@Slf4j
@Service
public class KakaoLoginService {
    private final KakaoLoginProperties kakaoLoginProperties;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final UserSocialRepository userSocialRepository;
    private final JwtService jwtService;
    private final OAuthImageService oAuthImageService;

    public KakaoLoginService(KakaoLoginProperties kakaoLoginProperties, ObjectMapper objectMapper, JwtService jwtService,
                             UserRepository userRepository, UserSocialRepository userSocialRepository, OAuthImageService oAuthImageService) {
        this.kakaoLoginProperties = kakaoLoginProperties;
        this.objectMapper = objectMapper;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userSocialRepository = userSocialRepository;
        this.oAuthImageService = oAuthImageService;
    }

    /**
     * 2. 토큰 얻기 단계
     *
     * @param code 인증 코드
     * @return 카카오 토큰 정보
     */
    public KakaoTokenResponseDto getToken(String code) {
        log.info("get token");
        // 토큰 요청 데이터 -> MultiValueMap
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoLoginProperties.getKakaoLoginApiKey());
        params.add("redirect_uri", kakaoLoginProperties.getRedirectUri());
        params.add("code", code);
        params.add("client_secret", kakaoLoginProperties.getKakaoClientSecret());


        // 웹 클라이언트로 요청보내기
        String response = WebClient.create(kakaoLoginProperties.getKakaoAuthBaseUri())
                .post()
                .uri(kakaoLoginProperties.getTokenRequestUri())
                .body(BodyInserters.fromFormData(params))
                .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8") //요청 헤더
                .retrieve()
                .bodyToMono(String.class)
                .block();

        //json 응답을 객체로 변환
        KakaoTokenResponseDto kakaoToken = null;

        try {
            kakaoToken = objectMapper.readValue(response, KakaoTokenResponseDto.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse Kakao token response", e);
        }

        return kakaoToken;
    }

    /**
     * 3. 회원 정보 요청
     * @param kakaoToken 카카오에서 받은 토큰
     * @return 카카오 회원 정보
     */
    public OAuth2User getUserInfo(KakaoTokenResponseDto kakaoToken) {
        // 액세스 토큰으로 ClientRegistration 생성
        ClientRegistration clientRegistration = getKakaoClientRegistration();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, kakaoToken.getAccessToken(), null, null);
        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);

        // CustomOAuth2UserService를 사용하여 사용자 정보 로드
        CustomOAuth2UserService userService = new CustomOAuth2UserService(userRepository, userSocialRepository, jwtService, oAuthImageService);
        OAuth2User oAuth2User = userService.loadUser(userRequest);

        return oAuth2User;
    }

    public boolean isGuest(CustomOAuth2User oAuth2User) {
        // Logic to determine if the user is a guest
        // For example, you might check roles or other attributes
        return oAuth2User.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_GUEST"));
    }

    private ClientRegistration getKakaoClientRegistration() {
        return ClientRegistration.withRegistrationId("kakao")
                .clientId("YOUR_KAKAO_CLIENT_ID")
                .clientSecret("YOUR_KAKAO_CLIENT_SECRET")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("profile_nickname", "profile_image")
                .redirectUri("http://localhost:8080/auth/code/kakao")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .clientName("Kakao")
                .build();
    }
}
