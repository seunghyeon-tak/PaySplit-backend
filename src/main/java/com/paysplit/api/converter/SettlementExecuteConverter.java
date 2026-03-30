package com.paysplit.api.converter;

import com.paysplit.api.dto.settlement.response.SettlementExecuteResponse;
import com.paysplit.common.annotation.Converter;
import com.paysplit.db.domain.Settlement;

@Converter
public class SettlementExecuteConverter {
    public SettlementExecuteResponse toResponse(Settlement settlement) {
        return SettlementExecuteResponse.builder()
                .settlementId(settlement.getId())
                .status(settlement.getStatus())
                .build();
    }
}
