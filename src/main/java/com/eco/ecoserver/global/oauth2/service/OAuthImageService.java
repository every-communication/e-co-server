package com.eco.ecoserver.global.oauth2.service;



import com.eco.ecoserver.domain.image.URLMultipartFile;
import com.eco.ecoserver.domain.image.service.S3UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthImageService {
    private final S3UploadService s3UploadService;

    public String transferSocialProfileImage(String socialImageUrl) {
        if (socialImageUrl == null || socialImageUrl.isEmpty()) {
            return null;
        }

        try {
            // URL에서 이미지 다운로드
            URL url = new URL(socialImageUrl);
            try (InputStream inputStream = url.openStream()) {
                MultipartFile multipartFile = new URLMultipartFile(
                        inputStream,
                        "profile" + System.currentTimeMillis() + ".jpg",
                        "image/jpeg"
                );

                // S3에 업로드
                ResponseEntity<?> response = s3UploadService.uploadResizedImage(
                        multipartFile,
                        "profiles",
                        200
                );

                if (response.getStatusCode().is2xxSuccessful()) {
                    return (String) response.getBody();
                } else {
                    log.error("Failed to upload profile image to S3. Status: {}, Message: {}",
                            response.getStatusCode(), response.getBody());
                    return socialImageUrl; // 실패 시 원본 URL 반환
                }
            }
        } catch (IOException e) {
            log.error("Error processing profile image from URL {}: ", socialImageUrl, e);
            return socialImageUrl; // 에러 발생 시 원본 URL 반환
        }
    }
}