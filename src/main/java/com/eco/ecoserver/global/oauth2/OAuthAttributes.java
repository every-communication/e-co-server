package com.eco.ecoserver.global.oauth2;

import com.eco.ecoserver.domain.image.URLMultipartFile;
import com.eco.ecoserver.domain.image.service.S3UploadService;
import com.eco.ecoserver.domain.user.Role;
import com.eco.ecoserver.domain.user.SocialType;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.UserSocial;
import com.eco.ecoserver.global.oauth2.service.OAuthImageService;
import com.eco.ecoserver.global.oauth2.userinfo.GoogleOAuth2UserInfo;
import com.eco.ecoserver.global.oauth2.userinfo.KakaoOAuth2UserInfo;
import com.eco.ecoserver.global.oauth2.userinfo.NaverOAuth2UserInfo;
import com.eco.ecoserver.global.oauth2.userinfo.OAuth2UserInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

/**
 * 각 소셜에서 받아오는 데이터가 다르므로
 * 소셜별로 데이터를 받는 데이터를 분기 처리하는 DTO 클래스
 */
@Builder
@Getter
@ToString
@RequiredArgsConstructor
public class OAuthAttributes {
    private final String nameAttributeKey;
    private final OAuth2UserInfo oauth2UserInfo;
    private final OAuthImageService oAuthImageService;

    public static OAuthAttributes of(SocialType socialType,
                                     String userNameAttributeName,
                                     Map<String, Object> attributes,
                                     OAuthImageService oAuthImageService) {
        switch (socialType) {
            case GOOGLE:
                return ofGoogle(userNameAttributeName, attributes, oAuthImageService);
            case KAKAO:
                return ofKakao(userNameAttributeName, attributes, oAuthImageService);
            case NAVER:
                return ofNaver(userNameAttributeName, attributes, oAuthImageService);
            default:
                throw new RuntimeException("Unsupported social type");
        }
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName,
                                            Map<String, Object> attributes,
                                            OAuthImageService oAuthImageService) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new GoogleOAuth2UserInfo(attributes))
                .oAuthImageService(oAuthImageService)
                .build();
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName,
                                           Map<String, Object> attributes,
                                           OAuthImageService oAuthImageService) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new KakaoOAuth2UserInfo(attributes))
                .oAuthImageService(oAuthImageService)
                .build();
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName,
                                           Map<String, Object> attributes,
                                           OAuthImageService oAuthImageService) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new NaverOAuth2UserInfo(attributes))
                .oAuthImageService(oAuthImageService)
                .build();
    }

    public User toEntity(SocialType socialType, OAuth2UserInfo oauth2UserInfo) {
        UserSocial userSocial = UserSocial.builder()
                .socialType(socialType)
                .socialId(oauth2UserInfo.getId())
                .build();

        // 소셜 프로필 이미지 URL을 S3에 업로드하고 새 URL 받기
        String originalImageUrl = oauth2UserInfo.getImageUrl();
        String thumbnailUrl = oAuthImageService.transferSocialProfileImage(originalImageUrl);

        User user = User.builder()
                .nickname(oauth2UserInfo.getNickname())
                .email(oauth2UserInfo.getId() + "@" + socialType.name().toLowerCase() + ".com")
                .thumbnail(thumbnailUrl)
                .role(Role.GUEST)
                .userSocial(userSocial)
                .build();

        userSocial.setUser(user);
        return user;
    }
}