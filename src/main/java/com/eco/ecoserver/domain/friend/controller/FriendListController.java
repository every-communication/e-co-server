package com.eco.ecoserver.domain.friend.controller;

import com.eco.ecoserver.domain.friend.dto.CreateFriendListDTO;
import com.eco.ecoserver.domain.friend.dto.FriendListDTO;
import com.eco.ecoserver.domain.friend.repository.FriendListRepository;
import com.eco.ecoserver.domain.friend.service.FriendListService;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.global.jwt.service.JwtService;

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
    public List<FriendListDTO> getFriendList(@RequestHeader("Authorization") String authHeader){

        String e = getUserEmail(authHeader);

        return friendListService.getFriendList(userService.getUserIdByEmail(e));
    }

    @PostMapping("/friends")
    public ResponseEntity<String> createFriends(@RequestBody Long friendId, @RequestHeader("Authorization")String authHeader){
        String e = getUserEmail(authHeader);
        CreateFriendListDTO createFriendListDTO = new CreateFriendListDTO(userService.getUserIdByEmail(e), friendId);
        Long friendListId =  friendListService.save(createFriendListDTO);
        return ResponseEntity.ok("Saved Friend List: "+friendListId);
    }

    @DeleteMapping("/friends/{friendListId}")
    public ResponseEntity<String> deleteFriends(@PathVariable("friendListId") Long id){
        friendListService.delete(id);
        return ResponseEntity.ok("Deleted Friend: "+id);
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
