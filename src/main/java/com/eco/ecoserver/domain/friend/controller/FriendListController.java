package com.eco.ecoserver.domain.friend.controller;

import com.eco.ecoserver.domain.friend.dto.CreateFriendListDTO;
import com.eco.ecoserver.domain.friend.dto.FriendListDTO;
import com.eco.ecoserver.domain.friend.repository.FriendListRepository;
import com.eco.ecoserver.domain.friend.service.FriendListService;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.dto.UserInfoDto;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.jwt.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class FriendListController {
    private final FriendListService friendListService;
    private final JwtService jwtService;
    private final UserService userService;
    @GetMapping("/friends")
    public ResponseEntity<ApiResponseDto<List<FriendListDTO>>> getFriendList(HttpServletRequest request){

        // request(token)에서 email 추출
        Optional<String> email = jwtService.extractEmailFromToken(request);

        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(403, "권한이 없습니다."));
        }
        // email로 찾은 user 반환
        Optional<User> user = userService.findByEmail(email.get());

        return user.map(value -> {
            List<FriendListDTO> friendListDTO = friendListService.getFriendList(value);
            return ResponseEntity.ok(ApiResponseDto.success(friendListDTO));
        }).orElseGet(() -> ResponseEntity.status(404).body(ApiResponseDto.failure(404, "사용자를 찾을 수 없습니다.")));
         }


    @DeleteMapping("/friends/{friendListId}")
    public ResponseEntity<ApiResponseDto<String>> deleteFriends(@PathVariable("friendListId") Long id){
        friendListService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.success("친구 삭제 완료"));
    }


}
