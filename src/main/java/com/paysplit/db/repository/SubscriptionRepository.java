package com.paysplit.db.repository;

import com.paysplit.db.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByPartyId(Long partyId);
}
