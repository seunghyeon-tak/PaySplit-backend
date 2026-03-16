package com.paysplit.common.error.subscription;

import lombok.Getter;

@Getter
public class SubscriptionException extends RuntimeException {
    private final SubscriptionErrorCode errorCode;

    public SubscriptionException(SubscriptionErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
