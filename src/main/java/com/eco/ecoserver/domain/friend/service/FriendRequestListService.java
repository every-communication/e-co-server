package com.eco.ecoserver.domain.friend.service;

import com.eco.ecoserver.domain.friend.FriendList;
import com.eco.ecoserver.domain.friend.FriendRequestList;
import com.eco.ecoserver.domain.friend.FriendState;
import com.eco.ecoserver.domain.friend.dto.CreateFriendListDTO;
import com.eco.ecoserver.domain.friend.dto.CreateFriendRequestListDTO;
import com.eco.ecoserver.domain.friend.dto.FriendListDTO;
import com.eco.ecoserver.domain.friend.exception.DuplicateFriendRequestException;
import com.eco.ecoserver.domain.friend.exception.UserNotFoundException;
import com.eco.ecoserver.domain.friend.repository.FriendRequestListRepository;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendRequestListService {
    private final FriendRequestListRepository friendRequestListRepository;
    private final UserRepository userRepository;
    private final FriendListService friendListService;

    @Transactional
    public CreateFriendRequestListDTO createFriendRequest(String searchUser, Long requestId) {
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
        checkDuplicate(requestId, user.getId());
        CreateFriendRequestListDTO createFriendRequestListDTO = new CreateFriendRequestListDTO(requestId, user.getId(), FriendState.SENDING);
        friendRequestListRepository.save(createFriendRequestListDTO.toEntity());
        return createFriendRequestListDTO;

    }

    @Transactional
    public List<FriendListDTO> getFriendRequestedList(Long userId){
        List<FriendRequestList> friendRequestList = friendRequestListRepository.findByFriendIdAndFriendState(userId, FriendState.SENDING);
        List<FriendListDTO> friendListDTOS = new ArrayList<>();
        for(FriendRequestList f : friendRequestList){
            Optional<User> getUser = userRepository.findById(f.getUserId());
            if(getUser.isPresent()){
                User user = getUser.get();
                FriendListDTO friendListDTO = new FriendListDTO(user.getId(), user.getEmail(), user.getNickname(), user.getThumbnail());
                friendListDTOS.add(friendListDTO);
            }
        }
        return friendListDTOS;
    }

    @Transactional
    public List<FriendListDTO> getFriendRequestList(Long userId){
        List<FriendRequestList> friendRequestList = friendRequestListRepository.findByUserIdAndFriendState(userId, FriendState.SENDING);
        List<FriendListDTO> friendListDTOS = new ArrayList<>();
        for(FriendRequestList f : friendRequestList){
            Optional<User> getUser = userRepository.findById(f.getFriendId());
            if(getUser.isPresent()){
                User user = getUser.get();
                FriendListDTO friendListDTO = new FriendListDTO(user.getId(), user.getEmail(), user.getNickname(), user.getThumbnail());
                friendListDTOS.add(friendListDTO);
            }
        }
        return friendListDTOS;
    }

    @Transactional
    public CreateFriendListDTO approveFriendRequest(Long userId, Long friendId){
        List<FriendRequestList> friendRequestLists =  friendRequestListRepository.findByUserIdAndFriendId(userId ,friendId);
        for(FriendRequestList f:friendRequestLists){
            f.updateState(FriendState.APPROVED);
        }
        CreateFriendListDTO createFriendListDTO = new CreateFriendListDTO(friendId, userId);
        friendListService.save(createFriendListDTO);
        return createFriendListDTO;

    }

    @Transactional
    public List<FriendRequestList> removeFriendRequest(Long userId, Long friendId){
        List<FriendRequestList> friendRequestLists =  friendRequestListRepository.findByUserIdAndFriendId(userId ,friendId);
        for(FriendRequestList f:friendRequestLists){
            f.updateState(FriendState.REMOVED);
        }
        return friendRequestLists;
    }

    private void checkDuplicate(Long requestId, Long friendId){
        List<FriendRequestList> friendRequestLists = new ArrayList<>(friendRequestListRepository.findByUserIdAndFriendId(requestId, friendId));
        friendRequestLists.addAll(friendRequestListRepository.findByUserIdAndFriendId(friendId, requestId));
        for(FriendRequestList f: friendRequestLists){
            if(!f.getFriendState().equals(FriendState.REMOVED)){
                throw new DuplicateFriendRequestException("이미 보낸 요청이거나, 받은 요청이거나, 친구입니다.");
            }
        }
    }

}
