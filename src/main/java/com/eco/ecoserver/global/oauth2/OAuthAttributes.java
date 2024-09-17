package com.eco.ecoserver.global.oauth2;

import com.eco.ecoserver.domain.user.Role;
import com.eco.ecoserver.domain.user.SocialType;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.UserSocial;
import com.eco.ecoserver.global.oauth2.userinfo.GoogleOAuth2UserInfo;
import com.eco.ecoserver.global.oauth2.userinfo.KakaoOAuth2UserInfo;
import com.eco.ecoserver.global.oauth2.userinfo.NaverOAuth2UserInfo;
import com.eco.ecoserver.global.oauth2.userinfo.OAuth2UserInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;
import java.util.UUID;

/**
 * 각 소셜에서 받아오는 데이터가 다르므로
 * 소셜별로 데이터를 받는 데이터를 분기 처리하는 DTO 클래스
 */
@Builder
@Getter
@ToString
public class OAuthAttributes {
    private String nameAttributeKey;
    private OAuth2UserInfo oauth2UserInfo;
    private String provider;

    /**
     * SocialType에 맞는 메소드 호출하여 OAuthAttributes 객체 반환
     * 파라미터 : userNameAttributeName -> OAuth2 로그인 시 키(PK)가 되는 값 / attributes : OAuth 서비스의 유저 정보들
     * 소셜별 of 메소드(ofGoogle, ofKaKao, ofNaver)들은 각각 소셜 로그인 API에서 제공하는
     * 회원의 식별값(id), attributes, nameAttributeKey를 저장 후 build
     */
    public static OAuthAttributes of(SocialType socialType, String userNameAttributeName, Map<String, Object> attributes) {
        switch (socialType) {
            case GOOGLE:
                return ofGoogle(userNameAttributeName, attributes);
            case KAKAO:
                return ofKakao(userNameAttributeName, attributes);
            case NAVER:
                return ofNaver(userNameAttributeName, attributes);
            default:
                throw new RuntimeException(); // TODO: 에러 메시지
        }
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new GoogleOAuth2UserInfo(attributes))
                .build();
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new NaverOAuth2UserInfo(attributes))
                .build();
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new KakaoOAuth2UserInfo(attributes))
                .build();
    }
    /**
     * of메소드로 OAuthAttributes 객체가 생성되어, 유저 정보들이 담긴 OAuth2UserInfo가 소셜 타입별로 주입된 상태
     * OAuth2UserInfo에서 socialId(식별값), nickname, imageUrl을 가져와서 build
     * email에는 UUID로 중복 없는 랜덤 값 생성
     * role은 GUEST로 설정
     */

    public User toEntity(SocialType socialType, OAuth2UserInfo oauth2UserInfo) {
        UserSocial userSocial = UserSocial.builder()
                .socialType(socialType)
                .socialId(oauth2UserInfo.getId())
                .build();

        // User 객체 생성 및 반환
        User user = User.builder()
                .nickname(oauth2UserInfo.getNickname())              // 닉네임
                .email(oauth2UserInfo.getId() + "@socialUser.com")                    // 이메일
                .thumbnail(oauth2UserInfo.getImageUrl())
                .role(Role.GUEST)                // 기본 역할 GUEST
                .userSocial(userSocial)          // UserSocial과 연결
                .build();

        // UserSocial의 User 설정 (양방향 관계를 위해)
        userSocial.setUser(user);
        return user;
    }
}