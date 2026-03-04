package com.paysplit.db.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PartyMemberStatus {
    ACTIVE("활성"),
    LEFT("자진탈퇴"),
    KICKED("강퇴"),
    ;

    private final String description;
}
