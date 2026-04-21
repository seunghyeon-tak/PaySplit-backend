package com.paysplit.api.dto.billing.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillingKeyResponse {
    @Schema(name = "user_id", description = "유저 ID", example = "1")
    private Long userId;

    @Schema(name = "customer_key", description = "구매자 ID", example = "aENcQAtPdYbTjGhtQnNVj")
    private String customerKey;

    @Schema(name = "card_number", description = "카드 번호", example = "12345678****123*")
    private String cardNumber;

    @Schema(name = "card_type", description = "카드 종류", example = "신용")
    private String cardType;

    @Schema(name = "authenticated_at", description = "결제 승인 시점", example = "2026-02-25T14:38:41+09:00")
    private LocalDateTime authenticatedAt;
}
