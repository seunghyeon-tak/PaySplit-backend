package com.paysplit.common.error.subscriptionplan;

import lombok.Getter;

@Getter
public class SubscriptionPlanException extends RuntimeException {
    private final SubscriptionPlanErrorCode errorCode;

    public SubscriptionPlanException(SubscriptionPlanErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
