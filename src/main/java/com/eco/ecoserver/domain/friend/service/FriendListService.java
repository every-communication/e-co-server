package com.eco.ecoserver.domain.friend.service;

import com.eco.ecoserver.domain.friend.FriendList;
import com.eco.ecoserver.domain.friend.FriendRequestList;
import com.eco.ecoserver.domain.friend.FriendState;
import com.eco.ecoserver.domain.friend.dto.CreateFriendListDTO;
import com.eco.ecoserver.domain.friend.dto.FriendListDTO;
import com.eco.ecoserver.domain.friend.exception.DuplicateFriendException;
import com.eco.ecoserver.domain.friend.exception.DuplicateFriendRequestException;
import com.eco.ecoserver.domain.friend.repository.FriendListRepository;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendListService {

    private final FriendListRepository friendListRepository;
    private final UserRepository userRepository;

    //친구 교차 저장
    @Transactional
    public Long save(CreateFriendListDTO createFriendListDTO){
        checkDuplicate(createFriendListDTO.getUserId(), createFriendListDTO.getFriendId());
        CreateFriendListDTO createFriendListDTOReversed = new CreateFriendListDTO(createFriendListDTO.getFriendId(),
                createFriendListDTO.getUserId());
        Long friendListId = friendListRepository.save(createFriendListDTO.toEntity()).getId();
        friendListRepository.save(createFriendListDTOReversed.toEntity());
        return friendListId;
    }

    private void checkDuplicate(Long userId, Long friendId){
        List<FriendList> friendLists =  friendListRepository.findByUserIdAndFriendId(userId, friendId);
        if(!friendLists.isEmpty())
            throw new DuplicateFriendException("이미 친구입니다.");


    }

    //친구 교차 삭제
    @Transactional
    public void delete(Long id){
        FriendList friendList = friendListRepository.getReferenceById(id);
        Long userId = friendList.getUserId();
        Long friendId = friendList.getFriendId();

        friendListRepository.deleteByUserIdAndFriendId(userId, friendId);
        friendListRepository.deleteByUserIdAndFriendId(friendId, userId);

    }


    @Transactional
    public List<FriendListDTO> getFriendList(Long userId){
        List<FriendList> friends = friendListRepository.findByUserId(userId);
        List<FriendListDTO> friendListDTOS = new ArrayList<>();
        for (FriendList friend : friends) {
            Optional<User> getUser = userRepository.findById(friend.getFriendId());
            //System.out.println(getUser.get());
            if(getUser.isPresent()){
                User user = getUser.get();
                FriendListDTO friendListDTO = new FriendListDTO(user.getEmail(), user.getNickname(), user.getThumbnail());
                friendListDTOS.add(friendListDTO);
            }

        }
        return friendListDTOS;
    }


}
