package com.paysplit.api.service;

import com.paysplit.api.dto.settlement.result.SettlementItemResult;
import com.paysplit.common.error.settlement.SettlementException;
import com.paysplit.db.domain.Payment;
import com.paysplit.db.domain.Settlement;
import com.paysplit.db.domain.SettlementItem;
import com.paysplit.db.enums.SettlementStatus;
import com.paysplit.db.enums.SettlementType;
import com.paysplit.db.repository.SettlementItemRepository;
import com.paysplit.db.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.paysplit.common.error.settlement.SettlementErrorCode.ALREADY_SETTLED_PAYMENT;

@Service
@RequiredArgsConstructor
public class SettlementService {
    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;

    /*
     * 멱등 보장
     * - 없으면 생성
     * - 동시성으로 유니크 충돌 나면 기존 settlement 조회해서 반환
     * */
    public Settlement createOrGetSettlement(Payment payment) {
        // 1) 먼저 조회해보고 있으면 반환 (빠른 경로)
        return settlementRepository.findByPayment(payment)
                .orElseGet(() -> createSettlementInternal(payment));
    }

    public Settlement createSettlementInternal(Payment payment) {
        // settlement Entity 생성
        Settlement settlement = Settlement.builder()
                .payment(payment)
                .settlementPolicy(payment.getSettlementPolicy())
                .status(SettlementStatus.READY)
                .type(SettlementType.NORMAL)
                .totalAmount(payment.getAmount())
                .build();

        try {
            // 저장
            return settlementRepository.save(settlement);
        } catch (DataIntegrityViolationException e) {
            // 동시성으로 다른 트랜잭션이 먼저 만들었으면 기존 거 반환
            return settlementRepository.findByPayment(payment)
                    .orElseThrow(() -> new SettlementException(ALREADY_SETTLED_PAYMENT));
        }

    }

    public void createSettlementItems(
            Settlement settlement, List<SettlementItemResult> results) {
//        for (SettlementItemResult result : results) {
//            SettlementItem item = SettlementItem.builder()
//                    .settlement(settlement)
//                    .receiverType(result.getReceiverType())
//                    .receiverId(result.getReceiverId())
//                    .amount(result.getAmount())
//                    .build();
//
//            settlementItemRepository.save(item);
//        }

        // stream
        List<SettlementItem> items = results.stream()
                .map(result -> SettlementItem.builder()
                        .settlement(settlement)
                        .receiverType(result.getReceiverType())
                        .receiverId(result.getReceiverId())
                        .amount(result.getAmount())
                        .build())
                .toList();

        settlementItemRepository.saveAll(items);
    }

    public void prepareRetry(Settlement settlement) {
        // 기존 아이템 제거(중복 방지)
        settlementItemRepository.deleteBySettlementId(settlement.getId());
    }
}
