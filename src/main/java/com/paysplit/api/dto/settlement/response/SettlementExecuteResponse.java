package com.paysplit.api.dto.settlement.response;

import com.paysplit.db.enums.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettlementExecuteResponse {
    @Schema(name = "settlement_id", description = "정산 ID", examples = "1")
    private Long settlementId;

    @Schema(description = "정산 상태", examples = "READY")
    private SettlementStatus status;
}
