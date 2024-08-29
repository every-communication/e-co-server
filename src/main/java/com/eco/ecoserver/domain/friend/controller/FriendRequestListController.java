package com.eco.ecoserver.domain.friend.controller;

import com.eco.ecoserver.domain.friend.dto.FriendRequestDTO;
import com.eco.ecoserver.domain.friend.repository.FriendRequestListRepository;
import com.eco.ecoserver.domain.friend.service.FriendRequestListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FriendRequestListController {

    private final FriendRequestListService friendRequestListService;

    @PostMapping("/friend-request")
    public ResponseEntity<String> createFriendRequest(@RequestBody FriendRequestDTO friendRequestDTO){
        Long id = friendRequestListService.createFriendRequest(friendRequestDTO.getSearchUser(), friendRequestDTO.getRequestId());
        return ResponseEntity.ok("Request Friend List Id: " + id);
    }
}
