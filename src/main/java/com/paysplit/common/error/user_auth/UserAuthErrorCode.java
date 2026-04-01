package com.paysplit.common.error.user_auth;

import com.paysplit.common.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserAuthErrorCode implements ErrorCode {
    USER_AUTH_NOT_FOUND(
            "USER_AUTH_001",
            "사용자 인증 정보를 찾을 수 없습니다",
            HttpStatus.NOT_FOUND
    ),
    INVALID_PASSWORD(
            "USER_AUTH_002",
            "비밀번호가 올바르지 않습니다",
            HttpStatus.UNAUTHORIZED
    ),
    INVALID_REFRESH_TOKEN(
            "USER_AUTH_003",
            "유효하지 않은 리프레쉬 토큰입니다",
            HttpStatus.UNAUTHORIZED
    ),
    INVALID_ACCESS_TOKEN(
            "USER_AUTH_004",
            "유효하지 않은 토큰입니다",
            HttpStatus.UNAUTHORIZED
    ),
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;

    UserAuthErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
