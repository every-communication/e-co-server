package com.eco.ecoserver.domain.friend.controller;

import com.eco.ecoserver.domain.friend.dto.FriendListDTO;
import com.eco.ecoserver.domain.friend.dto.FriendRequestDTO;
import com.eco.ecoserver.domain.friend.repository.FriendRequestListRepository;
import com.eco.ecoserver.domain.friend.service.FriendRequestListService;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.global.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class FriendRequestListController {

    private final FriendRequestListService friendRequestListService;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/friend-request")
    public ResponseEntity<String> createFriendRequest(@RequestBody String searchUser, @RequestHeader("Authorization") String authHeader) {

        String e = getUserEmail(authHeader);
        Long id = friendRequestListService.createFriendRequest(searchUser, userService.getUserIdByEmail(e));
        return ResponseEntity.ok("Request Friend List Id: " + id);
    }

    //친구 요청 수락
    @PostMapping("/friend-requested/approve")
    public Long approveFriendRequested(@RequestBody Long friendId, @RequestHeader("Authorization") String authHeader){
        String e = getUserEmail(authHeader);
        return friendRequestListService.approveFriendRequest(friendId, userService.getUserIdByEmail(e));
    }

    //친구 요청 거절
    @PatchMapping("/friend-requested/remove")
    public void removeFriendRequested(@RequestBody Long friendId, @RequestHeader("Authorization") String authHeader){
        String e = getUserEmail(authHeader);
        friendRequestListService.removeFriendRequest(friendId, userService.getUserIdByEmail(e));
    }

    //요청받은 친구목록
    @GetMapping("/friend-requested")
    public List<FriendListDTO> getFriendRequestedList(@RequestHeader("Authorization") String authHeader){
        String e =  getUserEmail(authHeader);
        return friendRequestListService.getFriendRequestedList(userService.getUserIdByEmail(e));
    }


    //요청한 친구목록
    @GetMapping("/friend-request")
    public List<FriendListDTO> getFriendRequestList(@RequestHeader("Authorization") String authHeader){
        String e =  getUserEmail(authHeader);
        return friendRequestListService.getFriendRequestList(userService.getUserIdByEmail(e));
    }


    //내가 요청한 친구 취소
    @PatchMapping("/friend-request/remove")
    public void removeFriendRequest(@RequestBody Long friendId, @RequestHeader("Authorization") String authHeader) {
        String e = getUserEmail(authHeader);
        friendRequestListService.removeFriendRequest(userService.getUserIdByEmail(e), friendId);
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
