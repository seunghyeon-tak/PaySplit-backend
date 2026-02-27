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
import com.paysplit.db.domain.Payment;
import com.paysplit.db.domain.Settlement;
import com.paysplit.db.enums.SettlementStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        // 2. Settlement 멱등 생성/조회
        Settlement settlement = settlementService.createOrGetSettlement(payment);

        // 3. 이미 처리된/진행중이면 그대로 반환 (멱등)
        if (settlement.getStatus() != SettlementStatus.READY) {
            return settlementExecuteConverter.toResponse(settlement);
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
            payment.settle();
        } catch (Exception e) {
            settlementFailureService.markFailed(settlement.getId());
            throw e;
        }

        return settlementExecuteConverter.toResponse(settlement);
    }
}
