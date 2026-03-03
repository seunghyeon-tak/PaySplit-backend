package com.paysplit.common.error.payment;

import com.paysplit.common.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PaymentErrorCode implements ErrorCode {
    PAYMENT_NOT_FOUND(
            "PAYMENT_001",
            "결제 정보를 찾을 수 없습니다",
            HttpStatus.NOT_FOUND
    ),
    INVALID_PAYMENT_STATE(
            "PAYMENT_002",
            "잘못된 결제 상태",
            HttpStatus.CONFLICT
    ),
    ALREADY_SETTLED(
            "PAYMENT_003",
            "이미 정산이 완료된 결제입니다",
            HttpStatus.CONFLICT
    );

    private final String code;
    private final String message;
    private final HttpStatus status;

    PaymentErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
