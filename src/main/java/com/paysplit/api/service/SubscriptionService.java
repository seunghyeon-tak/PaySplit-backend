package com.paysplit.api.service;

import com.paysplit.common.error.subscription.SubscriptionErrorCode;
import com.paysplit.common.error.subscription.SubscriptionException;
import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.Subscription;
import com.paysplit.db.domain.SubscriptionPlan;
import com.paysplit.db.enums.SubscriptionStatus;
import com.paysplit.db.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    public Subscription createSubscription(SubscriptionPlan plan, Party party) {
        Subscription subscription = Subscription.builder()
                .plan(plan)
                .party(party)
                .status(SubscriptionStatus.PENDING)
                .build();

        return subscriptionRepository.save(subscription);
    }

    public Subscription getByPartyId(Party party) {
        return subscriptionRepository.findByPartyId(party.getId())
                .orElseThrow(() -> new SubscriptionException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND));
    }

    public void subscriptionCancel(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND));

        subscription.cancel();
    }
}
