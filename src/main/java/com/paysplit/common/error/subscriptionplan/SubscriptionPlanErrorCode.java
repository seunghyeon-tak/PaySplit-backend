package com.paysplit.common.error.subscriptionplan;

import com.paysplit.common.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SubscriptionPlanErrorCode implements ErrorCode {
    PLAN_NOT_FOUND(
            "PLAN_001",
            "구독 플랜 정보를 찾을 수 없습니다",
            HttpStatus.NOT_FOUND
    ),
    PLAN_NOT_ACTIVE(
            "PLAN_002",
            "비활성화된 구독 플랜입니다",
            HttpStatus.BAD_REQUEST
    ),
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;

    SubscriptionPlanErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
