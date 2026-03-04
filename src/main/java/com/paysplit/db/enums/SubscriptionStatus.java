package com.paysplit.db.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubscriptionStatus {
    ACTIVE("활성"),
    EXPIRED("만료"),
    CANCELED("취소"),
    ;

    private final String description;
}
