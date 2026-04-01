package com.paysplit.common.error.user;

import com.paysplit.common.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(
            "USER_001",
            "사용자 정보를 찾을 수 없습니다",
            HttpStatus.NOT_FOUND
    ),
    LEFT_USER(
            "USER_002",
            "이미 탈퇴한 사용자 입니다",
            HttpStatus.BAD_REQUEST
    ),
    DUPLICATE_EMAIL(
            "USER_003",
            "이미 사용중인 이메일입니다",
            HttpStatus.BAD_REQUEST
    ),
    EMAIL_NOT_FOUND(
            "USER_004",
            "이메일이 존재하지 않습니다",
            HttpStatus.NOT_FOUND
    ),
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;

    UserErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
