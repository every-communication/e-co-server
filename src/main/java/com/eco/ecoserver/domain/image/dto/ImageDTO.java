package com.eco.ecoserver.domain.image.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ImageDTO {

    private String imageUrl;

    public ImageDTO(String imageUrl){
        this.imageUrl = imageUrl;
    }
}
