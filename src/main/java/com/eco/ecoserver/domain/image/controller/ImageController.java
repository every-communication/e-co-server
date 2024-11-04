package com.eco.ecoserver.domain.image.controller;

import com.eco.ecoserver.domain.image.dto.ImageDTO;
import com.eco.ecoserver.domain.image.service.S3UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ImageController {
    private final S3UploadService s3UploadService;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudfrontDomain;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestPart(value = "image") MultipartFile multipartFile) throws IOException {

        ResponseEntity<?> response = s3UploadService.uploadResizedImage(multipartFile, "images", 200);

        if (response.getStatusCode().is2xxSuccessful()) {
            String s3Url = (String) response.getBody();
            String cloudfrontUrl = convertToCloudFrontUrl(s3Url);

            log.info("Original S3 URL: {}", s3Url);
            log.info("CloudFront URL: {}", cloudfrontUrl);

            ImageDTO imageDTO = new ImageDTO(cloudfrontUrl);
            return ResponseEntity.ok()
                    .body(imageDTO);
        } else {
            String errorMessage = (String) response.getBody();
            HttpStatusCode statusCode = response.getStatusCode();

            log.error("Image upload failed. Status: {}, Message: {}", statusCode, errorMessage);

            return ResponseEntity
                    .status(statusCode)
                    .body("Image upload failed: " + errorMessage);
        }
    }

    private String convertToCloudFrontUrl(String s3Url) {
        // S3 URL에서 파일명 추출
        String filename = s3Url.substring(s3Url.lastIndexOf("/") + 1);
        // CloudFront URL 생성
        return String.format("https://%s/images/%s", cloudfrontDomain, filename);
    }
}