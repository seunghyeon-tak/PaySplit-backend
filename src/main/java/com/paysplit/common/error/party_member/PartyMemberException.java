package com.paysplit.common.error.party_member;

import lombok.Getter;

@Getter
public class PartyMemberException extends RuntimeException {
    private final PartyMemberErrorCode errorCode;

    public PartyMemberException(PartyMemberErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
