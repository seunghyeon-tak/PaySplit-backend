package com.paysplit.support;

import com.paysplit.db.domain.SettlementPolicy;
import com.paysplit.db.enums.FeeType;

import java.math.BigDecimal;

public class SettlementPolicyFixture {
    public static SettlementPolicy activePolicy() {
        return SettlementPolicy.builder()
                .policyCode("DEFAULT")
                .version(1)
                .platformFeeType(FeeType.RATE)
                .platformFeeValue(new BigDecimal("0.10"))
                .leaderShareType(FeeType.RATE)
                .leaderShareValue(new BigDecimal("0.05"))
                .active(true)
                .build();
    }
}
