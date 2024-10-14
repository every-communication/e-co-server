package com.eco.ecoserver.domain.image.controller;

import com.eco.ecoserver.domain.image.dto.ImageDTO;
import com.eco.ecoserver.domain.image.service.S3UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    @PostMapping(value ="/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestPart(value = "image") MultipartFile multipartFile) throws IOException {
        ResponseEntity<?> response =  s3UploadService.uploadResizedImage(multipartFile,"images", 200);
        if (response.getStatusCode().is2xxSuccessful()) {
            // 성공적인 업로드 처리
            String imageUrl = (String) response.getBody();
            // imageUrl을 사용하여 필요한 작업 수행
            return ResponseEntity.ok().body("Image uploaded successfully. URL: " + imageUrl);
        } else {
            // 오류 처리
            String errorMessage = (String) response.getBody();
            HttpStatus statusCode = (HttpStatus) response.getStatusCode();

            // 로그 기록
            log.error("Image upload failed. Status: {}, Message: {}", statusCode, errorMessage);

            // 클라이언트에게 오류 응답 전송
            return ResponseEntity
                    .status(statusCode)
                    .body("Image upload failed: " + errorMessage);
        }

    }
}
