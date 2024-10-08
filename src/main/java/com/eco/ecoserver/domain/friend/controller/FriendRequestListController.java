package com.eco.ecoserver.domain.friend.controller;

import com.eco.ecoserver.domain.friend.FriendRequestList;
import com.eco.ecoserver.domain.friend.dto.*;
import com.eco.ecoserver.domain.friend.repository.FriendRequestListRepository;
import com.eco.ecoserver.domain.friend.service.FriendRequestListService;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.global.dto.ApiResponseDto;
import com.eco.ecoserver.global.jwt.service.JwtService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Transactional
public class FriendRequestListController {

    private final FriendRequestListService friendRequestListService;
    private final JwtService jwtService;
    private final UserService userService;

    //친구 요청 위해 유저 검색
    @GetMapping("/friend-search")
    public ResponseEntity<ApiResponseDto<List<FriendSearchDTO>>> searchUser(@RequestParam String userInfo, HttpServletRequest request){
        // request(token)에서 email 추출
        Optional<String> email = jwtService.extractEmailFromToken(request);

        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed"));
        }
        // email로 찾은 user 반환
        Optional<User> user = userService.findByEmail(email.get());
        return user.map(value -> {
            System.out.println("user :"+ value.getEmail());
            List<FriendSearchDTO> friendSearchDTOS =  friendRequestListService.searchUsers(value, userInfo);
            return ResponseEntity.ok(ApiResponseDto.success(friendSearchDTOS));
        }).orElseGet(() -> ResponseEntity.status(404).body(ApiResponseDto.failure(404, "사용자를 찾을 수 없습니다.")));

    }

    //친구 요청
    @PostMapping("/friend-request/{friendId}")
    public ResponseEntity<ApiResponseDto<CreateFriendRequestListDTO>> createFriendRequest(@PathVariable Long friendId, HttpServletRequest request) {
        // request(token)에서 email 추출
        Optional<String> email = jwtService.extractEmailFromToken(request);

        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(403, "권한이 없습니다."));
        }
        // email로 찾은 user 반환
        Optional<User> user = userService.findByEmail(email.get());
        return user.map(value -> {
            CreateFriendRequestListDTO createFriendRequestListDTO = null;
            try {
                createFriendRequestListDTO = friendRequestListService.createFriendRequest(friendId, value.getId());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return ResponseEntity.ok(ApiResponseDto.success(createFriendRequestListDTO));
        }).orElseGet(() -> ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed")));


    }

    //친구 요청 수락
    @PostMapping("/friend-requested/approve")
    public ResponseEntity<ApiResponseDto<CreateFriendListDTO>> approveFriendRequested(@RequestBody Long userId, HttpServletRequest request){
        // request(token)에서 email 추출
        Optional<String> email = jwtService.extractEmailFromToken(request);

        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed"));
        }
        // email로 찾은 user 반환
        Optional<User> user = userService.findByEmail(email.get());
        return user.map(value -> {
            CreateFriendListDTO createFriendListDTO = friendRequestListService.approveFriendRequest(userId, value.getId());
            return ResponseEntity.ok(ApiResponseDto.success(createFriendListDTO));
        }).orElseGet(() -> ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed")));

    }

    //친구 요청 거절
    @PatchMapping("/friend-requested/remove")
    public ResponseEntity<ApiResponseDto<List<FriendRequestList>>> removeFriendRequested(@RequestBody Long friendId, HttpServletRequest request){
        // request(token)에서 email 추출
        Optional<String> email = jwtService.extractEmailFromToken(request);

        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed"));
        }
        // email로 찾은 user 반환
        Optional<User> user = userService.findByEmail(email.get());
        return user.map(value -> {
            List<FriendRequestList> friendRequestLists = friendRequestListService.removeFriendRequest(friendId, value.getId());
            return ResponseEntity.ok(ApiResponseDto.success(friendRequestLists));
        }).orElseGet(() -> ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed")));

    }

    //요청받은 친구목록
    @GetMapping("/friend-requested")
    public ResponseEntity<ApiResponseDto<List<FriendListDTO>>> getFriendRequestedList(HttpServletRequest request){
        // request(token)에서 email 추출
        Optional<String> email = jwtService.extractEmailFromToken(request);

        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed"));
        }
        // email로 찾은 user 반환
        Optional<User> user = userService.findByEmail(email.get());
        return user.map(value->{
            List<FriendListDTO> friendListDTOS = friendRequestListService.getFriendRequestedList(value.getId());
            return ResponseEntity.ok(ApiResponseDto.success(friendListDTOS));
        }).orElseGet(() -> ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed")));

    }



    //요청한 친구목록
    @GetMapping("/friend-request")
    public ResponseEntity<ApiResponseDto<List<FriendListDTO>>> getFriendRequestList(HttpServletRequest request){
        // request(token)에서 email 추출
        Optional<String> email = jwtService.extractEmailFromToken(request);

        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed"));
        }
        // email로 찾은 user 반환
        Optional<User> user = userService.findByEmail(email.get());
        return user.map(value->{
            List<FriendListDTO> friendListDTOS = friendRequestListService.getFriendRequestList(value.getId());
            return ResponseEntity.ok(ApiResponseDto.success(friendListDTOS));
        }).orElseGet(() -> ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed")));

    }


    //내가 요청한 친구 취소
    @PatchMapping("/friend-request/remove")
    public ResponseEntity<ApiResponseDto<List<FriendRequestList>>> removeFriendRequest(@RequestBody Long friendId, HttpServletRequest request) {
        // request(token)에서 email 추출
        Optional<String> email = jwtService.extractEmailFromToken(request);

        if (email.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed"));
        }
        // email로 찾은 user 반환
        Optional<User> user = userService.findByEmail(email.get());
        return user.map(value -> {
            List<FriendRequestList> friendRequestLists = friendRequestListService.removeFriendRequest(value.getId(), friendId);
            return ResponseEntity.ok(ApiResponseDto.success(friendRequestLists));
        }).orElseGet(() -> ResponseEntity.status(401).body(ApiResponseDto.failure(401, "Unauthorizaed")));

    }


        private String getUserEmail(String authHeader){
        // Authorization 헤더에서 "Bearer " 접두어 제거
        String token = authHeader.replace("Bearer ", "");
        Optional<String> email = jwtService.extractEmail(token);
        String e = "";
        if(email.isPresent()){
            e = email.get();
        }
        return e;
    }
}
