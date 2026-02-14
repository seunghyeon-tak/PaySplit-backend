package com.paysplit.support;

import com.paysplit.db.domain.Payment;
import com.paysplit.db.domain.SettlementPolicy;
import com.paysplit.db.enums.PaymentMethod;
import com.paysplit.db.enums.PaymentStatus;

import java.math.BigDecimal;

public class PaymentFixture {
    public static Payment completedPayment(SettlementPolicy policy) {
        return Payment.builder()
                .amount(new BigDecimal("10000"))
                .settlementPolicy(policy)
                .status(PaymentStatus.COMPLETED)
                .method(PaymentMethod.CARD)
                .payerId(1L)
                .currency("KRW")
                .externalPaymentId("test-123")
                .build();
    }
}
