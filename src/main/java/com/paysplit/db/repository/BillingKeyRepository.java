package com.paysplit.db.repository;

import com.paysplit.db.domain.BillingKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingKeyRepository extends JpaRepository<BillingKey, Long> {
}
