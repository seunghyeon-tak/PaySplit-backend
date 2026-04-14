package com.paysplit.support;

import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.Subscription;
import com.paysplit.db.domain.SubscriptionPlan;
import com.paysplit.db.enums.SubscriptionStatus;

public class SubscriptionFixture {
    public static Subscription activePlan(SubscriptionPlan plan, Party party) {
        return Subscription.builder()
                .plan(plan)
                .party(party)
                .status(SubscriptionStatus.ACTIVE)
                .build();
    }

    public static Subscription pendingPlan(SubscriptionPlan plan, Party party) {
        return Subscription.builder()
                .plan(plan)
                .party(party)
                .status(SubscriptionStatus.PENDING)
                .build();
    }
}
