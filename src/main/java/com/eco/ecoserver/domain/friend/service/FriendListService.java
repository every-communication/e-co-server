package com.eco.ecoserver.domain.friend.service;

import com.eco.ecoserver.domain.friend.FriendList;
import com.eco.ecoserver.domain.friend.dto.CreateFriendListDTO;
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
        CreateFriendListDTO createFriendListDTOReversed = new CreateFriendListDTO(createFriendListDTO.getFriendId(),
                createFriendListDTO.getUserId());
        Long friendListId = friendListRepository.save(createFriendListDTO.toEntity()).getId();
        friendListRepository.save(createFriendListDTOReversed.toEntity());
        return friendListId;
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
    public List<User> getFriendList(Long userId){
        List<FriendList> friends = friendListRepository.findByUserId(userId);
        List<User> friendsList = new ArrayList<>();
        for (FriendList friend : friends) {
            Optional<User> getUser = userRepository.findById(friend.getFriendId());
            System.out.println(getUser.get());
            getUser.ifPresent(friendsList::add);
        }
        return friendsList;
    }


}
