package com.paysplit.api.business;

import com.paysplit.api.converter.SettlementExecuteConverter;
import com.paysplit.api.dto.settlement.request.SettlementExecuteRequest;
import com.paysplit.api.dto.settlement.response.SettlementExecuteResponse;
import com.paysplit.api.dto.settlement.result.SettlementItemResult;
import com.paysplit.api.service.PaymentService;
import com.paysplit.api.service.SettlementFailureService;
import com.paysplit.api.service.SettlementPolicyService;
import com.paysplit.api.service.SettlementService;
import com.paysplit.common.annotation.Business;
import com.paysplit.common.error.payment.PaymentErrorCode;
import com.paysplit.common.error.payment.PaymentException;
import com.paysplit.common.error.settlement.SettlementException;
import com.paysplit.db.domain.Payment;
import com.paysplit.db.domain.Settlement;
import com.paysplit.db.enums.SettlementStatus;
import com.paysplit.db.enums.SettlementType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.paysplit.common.error.settlement.SettlementErrorCode.*;

@Slf4j
@Business
@Transactional
@RequiredArgsConstructor
public class SettlementExecuteBusiness {
    private final SettlementService settlementService;
    private final SettlementFailureService settlementFailureService;
    private final PaymentService paymentService;
    private final SettlementPolicyService settlementPolicyService;
    private final SettlementExecuteConverter settlementExecuteConverter;

    public SettlementExecuteResponse execute(SettlementExecuteRequest request) {
        // 1) 결제 정보 조회 (+ 비관락)
        Payment payment = paymentService.getByIdForUpdate(request.getPaymentId());

        SettlementType type = request.getType();

        // 2) 멱등 빠른 경로: 이미 COMPLETED/IN_PROGRESS면 즉시 반환
        Optional<Settlement> existing = settlementService.findByPaymentAndType(payment, type);
        if (existing.isPresent()) {
            Settlement s = existing.get();
            if (s.getStatus() == SettlementStatus.COMPLETED || s.getStatus() == SettlementStatus.IN_PROGRESS) {
                return settlementExecuteConverter.toResponse(s);
            }
        }

        // 3) NORMAL만 결제 유효성 검사
        if (type == SettlementType.NORMAL && !payment.isPayable()) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATE);
        }

        // 4) REVERSAL/ADJUSTMENT면 원본 정산 조회 + 검증
        Settlement original = null;
        if (type == SettlementType.REVERSAL || type == SettlementType.ADJUSTMENT) {
            if (request.getOriginalSettlementId() == null) {
                throw new SettlementException(ORIGINAL_SETTLEMENT_REQUIRED);
            }

            original = settlementService.getById(request.getOriginalSettlementId());

            if (original.getPayment() == null || original.getPayment().getId() == null
                    || !original.getPayment().getId().equals(payment.getId())) {
                throw new SettlementException(ORIGINAL_SETTLEMENT_PAYMENT_MISMATCH);
            }
        }

        // 5) payment + type 기준 멱등 생성/조회
        Settlement settlement = settlementService.createOrGetSettlement(payment, type, original);

        // 6) 생성/조회 결과가 COMPLETED/IN_PROGRESS면 멱등 반환(동시성 안전망)
        if (settlement.getStatus() == SettlementStatus.COMPLETED
                || settlement.getStatus() == SettlementStatus.IN_PROGRESS) {
            return settlementExecuteConverter.toResponse(settlement);
        }

        // 7) FAILED면 재시도: 기존 items 제거
        if (settlement.getStatus() == SettlementStatus.FAILED) {
            settlementService.prepareRetry(settlement);
        }

        try {
            settlement.changeStatus(SettlementStatus.IN_PROGRESS);

            List<SettlementItemResult> results = settlementPolicyService.calculate(settlement);

            settlementService.createSettlementItems(settlement, results);

            settlement.changeStatus(SettlementStatus.COMPLETED);

            if (type == SettlementType.NORMAL) {
                paymentService.settleIfNotSettled(payment.getId());
            }

        } catch (Exception e) {
            try {
                settlementFailureService.markFailed(settlement.getId());
            } catch (Exception markFailedException) {
                log.warn("Failed to mark settlement as FAILED. settlementId={}", settlement.getId(), markFailedException);
            }
            throw e;
        }

        return settlementExecuteConverter.toResponse(settlement);
    }
}
