package com.eco.ecoserver.domain.friend.exception;

public class DuplicateFriendException extends RuntimeException{
    public DuplicateFriendException(String message){
        super(message);
    }
}
