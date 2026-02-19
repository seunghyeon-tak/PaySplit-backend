package com.paysplit.api.service;

import com.paysplit.db.domain.Settlement;
import com.paysplit.db.enums.SettlementStatus;
import com.paysplit.db.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementFailureService {
    private final SettlementRepository settlementRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId).orElseThrow();

        settlement.changeStatus(SettlementStatus.FAILED);
    }
}
