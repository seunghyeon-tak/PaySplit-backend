package com.paysplit.common.error.settlement;

import com.paysplit.common.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SettlementErrorCode implements ErrorCode {

    SETTLEMENT_NOT_FOUND(
            "SETTLEMENT_001",
            "정산 정보를 찾을 수 없습니다",
            HttpStatus.NOT_FOUND
    ),

    INVALID_SETTLEMENT_STATE(
            "SETTLEMENT_002",
            "현재 상태에서는 정산을 진행할 수 없습니다",
            HttpStatus.BAD_REQUEST
    ),
    ALREADY_SETTLED_PAYMENT(
            "SETTLEMENT_003",
            "이미 정산이 완료된 결제입니다",
            HttpStatus.CONFLICT
    ),
    INVALID_DISTRIBUTION_AMOUNT(
            "SETTLEMENT_004",
            "정산 분배 금액이 올바르지 않습니다",
            HttpStatus.BAD_REQUEST
    ),
    INVALID_PARTY_SIZE(
            "SETTLEMENT_005",
            "정산 인원 수가 올바르지 않습니다",
            HttpStatus.BAD_REQUEST
    ),
    INTERNAL_SERVER_ERROR(
            "SETTLEMENT_006",
            "정산 금액 합계가 일치하지 않습니다",
            HttpStatus.INTERNAL_SERVER_ERROR
    )
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;

    SettlementErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
