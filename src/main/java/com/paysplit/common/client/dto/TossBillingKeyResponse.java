package com.paysplit.common.client.dto;

import lombok.Data;

@Data
public class TossBillingKeyResponse {
    private String mid;
    private String customerKey;
    private String authenticatedAt;
    private String method;
    private String billingKey;
    private TossCardInfo card;

    @Data
    public static class TossCardInfo {
        private String issuerCode;
        private String acquirerCode;
        private String number;
        private String cardType;
        private String ownerType;
    }
}
