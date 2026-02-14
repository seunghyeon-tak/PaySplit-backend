package com.paysplit.db.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {
    PENDING("결제 요청"),
    COMPLETED("결제 완료"),
    CANCELED("결제 취소"),
    FAILED("결제 실패"),
    REFUNDED("환불 완료"),
    SETTLED("정산 완료")
    ;

    private final String description;
}
