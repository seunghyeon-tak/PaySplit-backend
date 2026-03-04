package com.paysplit.db.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PartyStatus {
    RECRUITING("모집중"),
    ACTIVE("활성"),
    DISBANDED("해체"),
    ;

    private final String description;
}
