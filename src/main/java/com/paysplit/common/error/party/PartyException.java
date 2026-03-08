package com.paysplit.common.error.party;

import lombok.Getter;

@Getter
public class PartyException extends RuntimeException {
    private final PartyErrorCode errorCode;

    public PartyException(PartyErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
