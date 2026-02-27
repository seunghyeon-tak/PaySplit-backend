package com.paysplit.db.repository;

import com.paysplit.db.domain.SettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementItemRepository extends JpaRepository<SettlementItem, Long> {
    List<SettlementItem> findAllBySettlementId(Long settlementId);

    void deleteBySettlementId(Long settlementId);
}
