package com.eco.ecoserver.global.oauth2.service;

import com.eco.ecoserver.domain.user.SocialType;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.UserSocial;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.domain.user.repository.UserSocialRepository;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.eco.ecoserver.global.oauth2.CustomOAuth2User;
import com.eco.ecoserver.global.oauth2.OAuthAttributes;
import com.eco.ecoserver.global.oauth2.dto.OAuthRegistrationDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@ToString
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final UserSocialRepository userSocialRepository;
    private final JwtService jwtService;

    private static final String NAVER = "naver";
    private static final String KAKAO = "kakao";
    private static final String GOOGLE = "google";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("CustomOAuth2UserService.loadUser() 실행 - OAuth2 로그인 요청 진입");

        // OAuth2UserRequest 객체의 정보 출력
        log.info("Client Registration Id: {}", userRequest.getClientRegistration().getRegistrationId());
        log.info("Access Token: {}", userRequest.getAccessToken().getTokenValue());
        log.info("Authorization Exchange: {}", userRequest.getAdditionalParameters().toString());

        // 1. OAuth2 로그인 유저 정보를 가져옴
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        log.info("getAttributes : {}", oAuth2User.getAttributes());

        // 2. provider : google, naver, kakao
        String provider = userRequest.getClientRegistration().getRegistrationId();
        SocialType socialType = getSocialType(provider);
        log.info("provider : {}", provider);

        // 3. 필요한 정보를 provider에 따라 다르게 mapping
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName(); // OAuth2 로그인 시 키(PK)가 되는 값
        Map<String, Object> attributes = oAuth2User.getAttributes(); // 소셜 로그인에서 API가 제공하는 userInfo의 Json 값(유저 정보들)

        OAuthAttributes extractAttributes = OAuthAttributes.of(socialType, userNameAttributeName, attributes);
        log.info("oAuth2UserInfo");

        // 4. 유저가 저장되어 있는지 유저 정보 확인
        //    없으면 DB 저장 후 해당 유저를 저장
        //    있으면 해당 유저를 저장
        User createdUser = getUserOrCreate(extractAttributes, socialType);
        log.info("user : {}", createdUser.toString());

        // 5. UserDetails와 OAuth2User를 다중 상속한 CustomUserDetails
        return new CustomOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(createdUser.getRole().getKey())),
                attributes,
                extractAttributes.getNameAttributeKey(),
                createdUser.getEmail(),
                createdUser.getRole()
        );
    }

    private SocialType getSocialType(String provider) {
        if(GOOGLE.equals(provider)) return SocialType.GOOGLE;
        if(NAVER.equals(provider)) return SocialType.NAVER;
        if(KAKAO.equals(provider)) return SocialType.KAKAO;
        return null;
    }

    private User getUserOrCreate(OAuthAttributes attributes, SocialType socialType) {
        UserSocial userSocial = userSocialRepository.findBySocialTypeAndSocialId(socialType,
                attributes.getOauth2UserInfo().getId()).orElse(null);

        if(userSocial != null) {
            return userSocial.getUser();
        } else {
            return saveUser(attributes, socialType);
        }
    }

    /**
     * OAuthAttributes의 toEntity() 메소드를 통해 빌더로 User 객체 생성 후 반환
     * 생성된 User 객체를 DB에 저장 : socialType, socialId, email, role 값만 있는 상태
     */
    private User saveUser(OAuthAttributes attributes, SocialType socialType) {
        User createdUser = attributes.toEntity(socialType, attributes.getOauth2UserInfo());
        return userRepository.save(createdUser);
    }

    public void handleGuestLogin(HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException {
        String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());
        String redirectUrl = "localhost:3000/auth/oauth-register?id=" + oAuth2User.getEmail();
        response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
        response.sendRedirect(redirectUrl);
    }

    public void loginSuccess(HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException {
        String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());
        String refreshToken = jwtService.createRefreshToken();

        String redirectUrl = String.format("localhost:3000/auth/oauth-callback?accessToken=%s&refreshToken=%s",
                URLEncoder.encode(accessToken, StandardCharsets.UTF_8.name()),
                URLEncoder.encode(refreshToken, StandardCharsets.UTF_8.name()));

        userRepository.findByEmail(oAuth2User.getEmail())
                .ifPresent(user -> {
                    user.updateRefreshToken(refreshToken);
                    userRepository.saveAndFlush(user);
                });

        response.sendRedirect(redirectUrl);
    }
}