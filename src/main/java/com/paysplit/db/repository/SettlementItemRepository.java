package com.paysplit.db.repository;

import com.paysplit.db.domain.SettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementItemRepository extends JpaRepository<SettlementItem, Long> {
    void deleteBySettlementId(Long settlementId);
}
