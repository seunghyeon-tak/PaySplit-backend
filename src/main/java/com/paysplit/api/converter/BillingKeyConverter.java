package com.paysplit.api.converter;

import com.paysplit.api.dto.billing.response.BillingKeyResponse;
import com.paysplit.common.annotation.Converter;
import com.paysplit.db.domain.BillingKey;

import java.time.LocalDateTime;

@Converter
public class BillingKeyConverter {
    public BillingKeyResponse toResponse(BillingKey billingKey) {
        return BillingKeyResponse.builder()
                .userId(billingKey.getUser().getId())
                .customerKey(billingKey.getCustomerKey())
                .cardNumber(billingKey.getCardNumber())
                .cardType(billingKey.getCardType())
                .authenticatedAt(LocalDateTime.now())
                .build();
    }
}
