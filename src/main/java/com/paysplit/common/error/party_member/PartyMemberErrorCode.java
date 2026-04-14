package com.paysplit.common.error.party_member;

import com.paysplit.common.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PartyMemberErrorCode implements ErrorCode {
    PARTY_MEMBER_NOT_FOUND(
            "PARTY_MEMBER_001",
            "파티 멤버에 존재 하지 않습니다.",
            HttpStatus.NOT_FOUND
    ),
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;

    PartyMemberErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
