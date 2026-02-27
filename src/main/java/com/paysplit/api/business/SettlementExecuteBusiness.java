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
        // 1. 결제 정보 조회 (+ 비관락)
        Payment payment = paymentService.getByIdForUpdate(request.getPaymentId());

        if (!payment.isPayable()) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATE);
        }

        SettlementType type = request.getType();
        if (type == null) {
            throw new SettlementException(INVALID_SETTLEMENT_TYPE_REQUEST);
        }

        // 2. Settlement 멱등 생성/조회
        Settlement original = null;
        if (type == SettlementType.REVERSAL || type == SettlementType.ADJUSTMENT) {
            if (request.getOriginalSettlementId() == null) {
                throw new SettlementException(ORIGINAL_SETTLEMENT_REQUIRED);
            }

            original = settlementService.getById(request.getOriginalSettlementId());

            // 원본 정산과 결제 일치 검증
            if (original.getPayment() == null || original.getPayment().getId() == null
                    || !original.getPayment().getId().equals(payment.getId())) {
                throw new SettlementException(ORIGINAL_SETTLEMENT_PAYMENT_MISMATCH);
            }
        }

        // payment + type 기준 멱등 생성/조회
        Settlement settlement = settlementService.createOrGetSettlement(payment, type, original);

        // 3. 이미 처리된/진행중이면 그대로 반환 (멱등)
        if (settlement.getStatus() == SettlementStatus.COMPLETED
                || settlement.getStatus() == SettlementStatus.IN_PROGRESS) {
            return settlementExecuteConverter.toResponse(settlement);
        }

        // 재시도 허용 (items 삭제 후 실행)
        if (settlement.getStatus() == SettlementStatus.FAILED) {
            settlementService.prepareRetry(settlement);
        }

        try {
            // 4. 정산 시작
            settlement.changeStatus(SettlementStatus.IN_PROGRESS);

            // 5. 정산 정책 적용 (금액 분배 계산)
            List<SettlementItemResult> results = settlementPolicyService.calculate(settlement);

            // 6. SettlementItem 생성 및 저장
            settlementService.createSettlementItems(settlement, results);

            // 7. 완료 처리
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
