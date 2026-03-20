package com.paysplit.common.error.party;

import com.paysplit.common.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PartyErrorCode implements ErrorCode {
    INVITE_CODE_GENERATE_FAILED(
            "PARTY_001",
            "초대 코드 생성 실패",
            HttpStatus.BAD_REQUEST
    ),
    PARTY_NOT_FOUND(
            "PARTY_002",
            "파티가 존재 하지 않습니다",
            HttpStatus.NOT_FOUND
    ),
    PARTY_MEMBER_FULL(
            "PARTY_003",
            "파티멤버가 가득 찼습니다.",
            HttpStatus.BAD_REQUEST
    ),
    ALREADY_JOINED(
            "PARTY_004",
            "이미 가입 했습니다.",
            HttpStatus.CONFLICT
    ),
    ;
    private final String code;
    private final String message;
    private final HttpStatus status;

    PartyErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
