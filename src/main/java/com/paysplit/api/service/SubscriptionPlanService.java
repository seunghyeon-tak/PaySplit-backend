package com.paysplit.api.service;

import com.paysplit.common.error.subscriptionplan.SubscriptionPlanException;
import com.paysplit.db.domain.SubscriptionPlan;
import com.paysplit.db.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.paysplit.common.error.subscriptionplan.SubscriptionPlanErrorCode.PLAN_NOT_ACTIVE;
import static com.paysplit.common.error.subscriptionplan.SubscriptionPlanErrorCode.PLAN_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public SubscriptionPlan getById(Long planId) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new SubscriptionPlanException(PLAN_NOT_FOUND));

        if (!plan.isActive()) {
            throw new SubscriptionPlanException(PLAN_NOT_ACTIVE);
        }

        return plan;
    }
}
