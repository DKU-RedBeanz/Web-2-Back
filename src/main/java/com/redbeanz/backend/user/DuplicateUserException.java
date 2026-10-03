package com.redbeanz.backend.user;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException() {
        super("이미 사용 중인 아이디 또는 이메일입니다.");
    }
}
