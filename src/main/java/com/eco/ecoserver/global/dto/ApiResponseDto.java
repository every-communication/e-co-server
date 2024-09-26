package com.eco.ecoserver.global.dto;

import lombok.Getter;

@Getter
public class ApiResponseDto<T> {
    // Getters and Setters
    private final int status;
    private final String message;   // 응답 메시지
    private final T data;           // 성공 시 반환할 데이터

    public ApiResponseDto(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
    public static <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>(200, "성공", data);
    }
    public static <T> ApiResponseDto<T> failure(String message) {
        return new ApiResponseDto<>(400, message, null);
    }

    // 특정 Error Status 를 보낼 때
    public static <T> ApiResponseDto<T> failure(int status, String message) {
        return new ApiResponseDto<>(status, message, null);
    }

}
