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
                .name("넷플릭스 스탠다드")
                .price(BigDecimal.valueOf(19000))
                .maxMembers(4)
                .build();
    }

    public static SubscriptionPlan recuitingParty(SettlementPolicy policy, Platform platform) {
        return SubscriptionPlan.builder()
                .policy(policy)
                .platform(platform)
                .name("유튜브 프리미엄")
                .price(BigDecimal.valueOf(1000))
                .maxMembers(1)
                .build();
    }
}
