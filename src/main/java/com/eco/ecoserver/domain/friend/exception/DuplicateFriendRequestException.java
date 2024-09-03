package com.eco.ecoserver.domain.friend.exception;

public class DuplicateFriendRequestException extends RuntimeException{
    public DuplicateFriendRequestException(String message){
        super(message);
    }
}
