package com.paysplit.api.dto.settlement.request;

import com.paysplit.db.enums.SettlementType;
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
    @NotNull
    private Long paymentId;

    @NotNull
    private SettlementType type;

    private Long originalSettlementId;
}
