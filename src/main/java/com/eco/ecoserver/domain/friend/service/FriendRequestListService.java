package com.eco.ecoserver.domain.friend.service;

import com.eco.ecoserver.domain.friend.FriendState;
import com.eco.ecoserver.domain.friend.dto.CreateFriendRequestListDTO;
import com.eco.ecoserver.domain.friend.exception.UserNotFoundException;
import com.eco.ecoserver.domain.friend.repository.FriendRequestListRepository;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendRequestListService {
    private final FriendRequestListRepository friendRequestListRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createFriendRequest(String searchUser, Long requestId){
        Optional<User> userFindByNickname= userRepository.findByNickname(searchUser);
        Optional<User> userFindByEmail = userRepository.findByEmail(searchUser);
        User user;
        if (userFindByNickname.isPresent()) {
            user = userFindByNickname.get();
        } else if (userFindByEmail.isPresent()) {
            user = userFindByEmail.get();
        } else {
            throw new UserNotFoundException("User with identifier " + searchUser + " not found.");
        }
        CreateFriendRequestListDTO createFriendRequestListDTO = new CreateFriendRequestListDTO(requestId, user.getId(), FriendState.SENDING);
        return friendRequestListRepository.save(createFriendRequestListDTO.toEntity()).getId();
    }

}
