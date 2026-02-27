package com.paysplit.api.service;

import com.paysplit.common.error.settlement.SettlementErrorCode;
import com.paysplit.common.error.settlement.SettlementException;
import com.paysplit.db.domain.Settlement;
import com.paysplit.db.enums.SettlementStatus;
import com.paysplit.db.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementFailureService {
    private final SettlementRepository settlementRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long settlementId) {
        Settlement settlement = settlementRepository
                .findById(settlementId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));

        try {
            settlement.changeStatus(SettlementStatus.FAILED);
        } catch (Exception e) {
            log.warn("Failed to change settlement status to FAILED. settlementId={}, currentStatus={}",
                    settlementId, settlement.getStatus(), e);
            throw e;
        }

    }
}
