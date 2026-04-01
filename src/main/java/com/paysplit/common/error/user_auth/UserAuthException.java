package com.paysplit.common.error.user_auth;

import lombok.Getter;

@Getter
public class UserAuthException extends RuntimeException {
    private final UserAuthErrorCode errorCode;

    public UserAuthException(UserAuthErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
