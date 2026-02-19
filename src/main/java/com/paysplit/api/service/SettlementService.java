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

import java.math.BigDecimal;
import java.util.List;

import static com.paysplit.common.error.settlement.SettlementErrorCode.ALREADY_SETTLED_PAYMENT;
import static com.paysplit.common.error.settlement.SettlementErrorCode.INVALID_SETTLEMENT_STATE;

@Service
@RequiredArgsConstructor
public class SettlementService {
    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;

    public Settlement createSettlement(Payment payment) {
        // 이미 정산된 결제인지 확인
        if (settlementRepository.existsByPayment(payment)) {
            throw new SettlementException(ALREADY_SETTLED_PAYMENT);
        }

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
            // db unique 위반 시 도메인 예외로 변환
            throw new SettlementException(ALREADY_SETTLED_PAYMENT);
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
        List<SettlementItem> items = results.stream().map(result -> SettlementItem.builder()
                        .settlement(settlement)
                        .receiverType(result.getReceiverType())
                        .receiverId(result.getReceiverId())
                        .amount(result.getAmount())
                        .build())
                .toList();

        settlementItemRepository.saveAll(items);
    }
}
