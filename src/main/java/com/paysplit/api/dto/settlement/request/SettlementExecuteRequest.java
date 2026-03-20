package com.paysplit.api.dto.settlement.request;

import com.paysplit.db.enums.SettlementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettlementExecuteRequest {
    @Schema(name = "payment_id", description = "결제 ID", examples = "1")
    @NotNull
    private Long paymentId;

    @Schema(description = "정산 상태", examples = "NORMAL")
    @NotNull
    private SettlementType type;

    @Schema(name = "original_settlement_id", description = "원본 정산 ID (취소 / 재정산 시 사용)", examples = "null")
    private Long originalSettlementId;
}
