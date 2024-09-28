package com.eco.ecoserver.domain.friend.service;

import com.eco.ecoserver.domain.friend.FriendList;
import com.eco.ecoserver.domain.friend.FriendRequestList;
import com.eco.ecoserver.domain.friend.FriendState;
import com.eco.ecoserver.domain.friend.FriendType;
import com.eco.ecoserver.domain.friend.dto.CreateFriendListDTO;
import com.eco.ecoserver.domain.friend.dto.CreateFriendRequestListDTO;
import com.eco.ecoserver.domain.friend.dto.FriendListDTO;
import com.eco.ecoserver.domain.friend.dto.FriendSearchDTO;
import com.eco.ecoserver.domain.friend.exception.DuplicateFriendRequestException;
import com.eco.ecoserver.domain.friend.exception.UserNotFoundException;
import com.eco.ecoserver.domain.friend.repository.FriendListRepository;
import com.eco.ecoserver.domain.friend.repository.FriendRequestListRepository;
import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendRequestListService {
    private final FriendRequestListRepository friendRequestListRepository;
    private final UserRepository userRepository;
    private final FriendListService friendListService;
    private final FriendListRepository friendListRepository;


    @Transactional
    public List<FriendSearchDTO> searchUsers(User user, String searchUser) {
        // 닉네임 또는 이메일에 searchUser 문자열이 포함된 유저 리스트 검색
        List<User> usersByNickname = userRepository.findByNicknameContainingIgnoreCase(searchUser);
        List<User> usersByEmail = userRepository.findByEmailContainingIgnoreCase(searchUser);

        // 두 리스트 합치기 (중복 제거)
        Set<User> users = new HashSet<>(usersByNickname);
        users.addAll(usersByEmail);
        //System.out.println("user:" + users.toArray());

        List<FriendSearchDTO> friendSearchDTOS = new ArrayList<>();
        if(!users.isEmpty()){
            for(User u : users){
                boolean friend = false;
                List<FriendList> friendList = friendListRepository.findByUserIdAndFriendId(user.getId(), u.getId());
                if(!friendList.isEmpty()) {
                    friendSearchDTOS.add(new FriendSearchDTO(u.getId(), u.getEmail(), u.getNickname(), u.getThumbnail(), FriendType.FRIEND));
                    friend = true;
                }
                List<FriendListDTO> friendListDTOS =  getFriendRequestList(user.getId());
                for(FriendListDTO f: friendListDTOS){
                    if(f.getUserId().equals(u.getId())){
                        friendSearchDTOS.add(new FriendSearchDTO(u.getId(), u.getEmail(), u.getNickname(), u.getThumbnail(), FriendType.REQUESTED));
                        friend = true;
                    }
                }
                List<FriendListDTO> friendListDTOS1 =  getFriendRequestedList(user.getId());
                for(FriendListDTO f: friendListDTOS1){
                    if(f.getUserId().equals(u.getId())){
                        friendSearchDTOS.add(new FriendSearchDTO(u.getId(), u.getEmail(), u.getNickname(), u.getThumbnail(), FriendType.RECEIVED));
                        friend = true;
                    }
                }
                if(!friend){
                    friendSearchDTOS.add(new FriendSearchDTO(u.getId(), u.getEmail(), u.getNickname(), u.getThumbnail(), FriendType.DEFAULT));
                }
            }


        }

        // 유저 리스트 반환
        return friendSearchDTOS;
    }

    @Transactional
    public CreateFriendRequestListDTO createFriendRequest(Long selectedUserId, Long requestId) {

        // 중복 확인
        checkDuplicate(requestId, selectedUserId);

        // 친구 요청 생성
        CreateFriendRequestListDTO createFriendRequestListDTO = new CreateFriendRequestListDTO(requestId, selectedUserId, FriendState.SENDING);
        friendRequestListRepository.save(createFriendRequestListDTO.toEntity());

        return createFriendRequestListDTO;
    }


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
            //System.out.println("friend: " +f.getFriendId());
            friendRequestLists.remove(f);
            break;
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
        List<FriendList> friendLists = friendListRepository.findByUserIdAndFriendId(requestId, friendId);
        if(!friendLists.isEmpty()){
            throw new DuplicateFriendRequestException("이미 친구입니다.");
        }
        List<FriendRequestList> friendRequestLists = new ArrayList<>(friendRequestListRepository.findByUserIdAndFriendId(requestId, friendId));
        friendRequestLists.addAll(friendRequestListRepository.findByUserIdAndFriendId(friendId, requestId));
        for(FriendRequestList f: friendRequestLists){
            if(!f.getFriendState().equals(FriendState.REMOVED)){
                throw new DuplicateFriendRequestException("이미 보낸 요청이거나, 받은 요청입니다.");
            }
        }
    }

}
