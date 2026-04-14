package com.paysplit.db.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LeaveStatus {
    IMMEDIATE("즉시 탈퇴"),
    RESERVED("탈퇴 예약"),
    ;
    private final String description;
}
