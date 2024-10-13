package com.eco.ecoserver.domain.image.controller;

import com.eco.ecoserver.domain.image.dto.ImageDTO;
import com.eco.ecoserver.domain.image.service.S3UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class ImageController {
    private final S3UploadService s3UploadService;

    @PostMapping(value ="/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ImageDTO uploadFile(
            @RequestPart(value = "image") MultipartFile multipartFile) throws IOException {
        String url = s3UploadService.uploadResizedImage(multipartFile,"images", 200);
        int index = url.indexOf("com");
        String result = "https://images.e-co.rldnd.net"+url.substring(index+3);
        return new ImageDTO(result);

    }
}
