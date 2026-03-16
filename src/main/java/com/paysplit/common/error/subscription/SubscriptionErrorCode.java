package com.paysplit.common.error.subscription;

import com.paysplit.common.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SubscriptionErrorCode implements ErrorCode {
    SUBSCRIPTION_NOT_FOUND(
            "SUBSCRIPTION_001",
            "구독이 존재하지 않습니다",
            HttpStatus.NOT_FOUND
    ),
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;

    SubscriptionErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
