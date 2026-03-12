package com.paysplit.support;

import com.paysplit.db.domain.Platform;
import com.paysplit.db.domain.SettlementPolicy;
import com.paysplit.db.domain.SubscriptionPlan;

import java.math.BigDecimal;

public class SubscriptionPlanFixture {
    public static SubscriptionPlan activePlan(SettlementPolicy policy, Platform platform) {
        return SubscriptionPlan.builder()
                .policy(policy)
                .platform(platform)
                .name("??")
                .price(BigDecimal.valueOf(19000))
                .maxMembers(4)
                .build();
    }
}
